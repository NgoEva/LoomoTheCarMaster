package com.segway.loomo.services;

import android.content.Context;
import android.util.Log;

import com.segway.loomo.RequestHandler;
import com.segway.loomo.objects.CarModel;
import com.segway.loomo.objects.Category;
import com.segway.loomo.objects.MapObject;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.grammar.GrammarConstraint;
import com.segway.robot.sdk.voice.grammar.Slot;
import com.segway.robot.sdk.voice.recognition.RecognitionListener;
import com.segway.robot.sdk.voice.recognition.RecognitionResult;

import java.util.ArrayList;
import java.util.Arrays;

public class RecognitionService extends Service {
    private static final String TAG = "RecognitionService";
    private final Context context;

    private Recognizer recognizer;
    private static RecognitionService instance;

    private RecognitionListener recognitionListener;

    private GrammarConstraint yesNoSlotGrammer;
    private GrammarConstraint categorySlotGrammar;
    private GrammarConstraint modelSlotGrammar;
    private GrammarConstraint generalInformationSlotGrammar;
    private GrammarConstraint questionInformationSlotGrammar;
    private GrammarConstraint additionalConsultationSlotGrammar;

    private boolean resetPosition = false;

    private boolean customerInterested = false;
    private boolean categorySelected = false;
    private boolean modelSelected = false;
    private boolean nextCar = false;
    private boolean moreInformation = false;
    private boolean callSalesman = false;
    private boolean additionalConsultation = false;
    private boolean contactInformation = false;

    private ArrayList<Category> categories;
    private ArrayList<CarModel> carModels;
    private ArrayList<MapObject> mapObjects;

    private ArrayList<MapObject> optionsCar = new ArrayList<>();
    private MapObject selectedCar;

    public static RecognitionService getInstance() {
        Log.d(TAG, "get recognizer instance");
        if (instance == null) {
            throw new IllegalStateException("RecognitionService instance not initialized yet");
        }
        return instance;
    }

    public RecognitionService(Context context) {
        Log.d(TAG, "recognition service initiated");
        this.context = context;
        this.init();
        this.initListeners();
        instance = this;
    }

    @Override
    public void init() {
        recognizer = Recognizer.getInstance();
        recognizer.bindService(this.context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "recognizer service bound successfully");
                initControlGrammer();
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "recognizer service unbound");
            }
        });
    }

    public void initListeners(){

        recognitionListener = new RecognitionListener() {
            @Override
            public void onRecognitionStart() {
                Log.i(TAG, "recognition started");
            }

            @Override
            public boolean onRecognitionResult(RecognitionResult recognitionResult) {
                //show the recognition result and recognition result confidence.
                Log.d(TAG, "recognition result: " + recognitionResult.getRecognitionResult() +
                        ", confidence:" + recognitionResult.getConfidence());
                String result = recognitionResult.getRecognitionResult();

                if (!(customerInterested && categorySelected && modelSelected && nextCar && moreInformation && callSalesman && additionalConsultation && contactInformation)) {
                    if (result.contains("yes") || result.contains("yeah") || result.contains("of course") || result.contains("sure")) {
                        Log.d(TAG, "customer is interested");
                        customerInterested = true;
                        categories = (ArrayList<Category>) RequestHandler.getInstance().makeRequest(RequestHandler.Collection.CATEGORIES);
                        carModels = (ArrayList<CarModel>) RequestHandler.getInstance().makeRequest(RequestHandler.Collection.CAR_MODELS);
                        mapObjects = (ArrayList<MapObject>) RequestHandler.getInstance().makeRequest(RequestHandler.Collection.SHOWROOM_MAP);
                        try {
                            recognizer.removeGrammarConstraint(yesNoSlotGrammer);
                            recognizer.addGrammarConstraint(categorySlotGrammar);
                            SpeakService.getInstance().speak("What category are you interested in?");
                            for (Category cat : categories) {
                                SpeakService.getInstance().speak(cat.getName());
                            }
                            return true;
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        return true;
                    } else if (result.contains("no") || result.contains("nah") || result.contains("nope")) {
                        Log.d(TAG, "customer not interested");
                        SpeakService.getInstance().speak("Alright. Thank you and have a nice day!");
                    }
                }

                else if (nextCar) {
                    if (result.contains("yes") || result.contains("yeah") || result.contains("of course") || result.contains("sure")) {
                        try {
                            nextCar = false;
                            customerInterested = true;
                            recognizer.removeGrammarConstraint(yesNoSlotGrammer);
                            recognizer.addGrammarConstraint(categorySlotGrammar);
                            return true;
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        SpeakService.getInstance().speak("What category are you interested in?");
                        for (Category cat : categories) {
                            SpeakService.getInstance().speak(cat.getName());
                        }
                        return true;
                    }
                    else if (result.contains("no") || result.contains("nah") || result.contains("nope")) {
                        nextCar = false;
                        callSalesman = true;
                        SpeakService.getInstance().speak("Alright. If you have more questions, you could talk to a human salesman now. Should I call someone?");
                        return true;
                    }
                    return true;
                }

                else if (callSalesman) {
                    if (result.contains("yes") || result.contains("yeah") || result.contains("of course") || result.contains("sure")) {
                        callSalesman = false;
                        try {
                            recognizer.removeGrammarConstraint(yesNoSlotGrammer);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        RecognitionService.getInstance().stopListening();
                        SpeakService.getInstance().speak("Okay, I will call a salesman. Please wait here, it will only take some minutes.");
                        return false;
                    }
                    else if (result.contains("no") || result.contains("nah") || result.contains("nope")) {
                        callSalesman = false;
                        additionalConsultation = true;
                        try {
                            recognizer.addGrammarConstraint(additionalConsultationSlotGrammar);
                            return true;
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        SpeakService.getInstance().speak("Alright, Do you want to get additional consultation? We could offer you a phone call," +
                                        " an appointment for a test drive or a sales offer.");
                        return true;
                    }
                    return true;
                }

                else if (additionalConsultation) {
                    if (result.contains("no") || result.contains("nah") || result.contains("nope")) {
                        additionalConsultation = false;
                        try {
                            recognizer.removeGrammarConstraint(yesNoSlotGrammer);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        RecognitionService.getInstance().stopListening();
                        SpeakService.getInstance().speak("Okay. Thank you! See you next time!");
                        return false;
                    }
                    else if (result.contains("offer") || result.contains("phone call") || result.contains("test drive")) {
                        if (result.contains("offer")) {
                            //write offer into database
                            return true;
                        }
                        else if (result.contains("phone call")) {
                            //write phone call into database
                            return true;
                        }
                        else if (result.contains("test drive")) {
                            //write test drive into database
                            return true;
                        }
                        try {
                            additionalConsultation = false;
                            contactInformation = true;
                            recognizer.removeGrammarConstraint(additionalConsultationSlotGrammar);
                            return true;
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        SpeakService.getInstance().speak("We need to collect some information from you to make an appointment or" +
                                "to send you an offer. Are you okay with that?");
                        return true;
                    }
                    return true;
                }

                else if (contactInformation) {
                    if (result.contains("yes") || result.contains("yeah") || result.contains("of course") || result.contains("sure")) {
                        contactInformation = false;
                        try {
                            recognizer.removeGrammarConstraint(yesNoSlotGrammer);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        RecognitionService.getInstance().stopListening();
                        SpeakService.getInstance().speak("Please enter your contact information on the screen and confirm.");
                        //show contact form --> on send button pressed: SpeakService.getInstance().speak("Thank you! We will contact you as soon as possible. Goodbye and have a nice day!");
                        return false;
                    }
                    else if (result.contains("no") || result.contains("nah") || result.contains("nope")) {
                        contactInformation = false;
                        try {
                            recognizer.removeGrammarConstraint(yesNoSlotGrammer);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        RecognitionService.getInstance().stopListening();
                        SpeakService.getInstance().speak("Okay. We are sorry that we cannot offer you additional consultation without your contact information. Thank you and see you next time!");
                    }
                }

                else if (customerInterested) {
                    Log.d(TAG, "customer is selecting category");
                    for (Category cat : categories) {
                        if (result.contains(cat.getName())) {
                            Log.d(TAG, cat.getName() + " is selected");
                            filterMapObjectsByCategory(cat);
                            customerInterested = false;
                            categorySelected = true;
                            try {
                                recognizer.removeGrammarConstraint(categorySlotGrammar);
                                recognizer.addGrammarConstraint(modelSlotGrammar);
                                SpeakService.getInstance().speak("Alright. Please tell me which model I should show you. Available models for this category are: ");
                                for (MapObject obj : optionsCar) {
                                    SpeakService.getInstance().speak(obj.getCar().getCarModel().getName());
                                }
                                return true;
                            } catch (VoiceException e) {
                                Log.e(TAG, "Exception: ", e);
                            }
                            return true;
                        }
                    }
                    return true;
                }

                else if (categorySelected) {
                    Log.d(TAG, "customer is selecting car model");
                    for (CarModel model : carModels) {
                        if (result.contains(model.getName())) {
                            Log.d(TAG, model.getName() + " is selected");
                            filterMapObjectsByCarModel(model);
                            categorySelected = false;
                            modelSelected = true;
                            try {
                                selectedCar = optionsCar.get(0);
                                recognizer.removeGrammarConstraint(modelSlotGrammar);
                                recognizer.addGrammarConstraint(generalInformationSlotGrammar);
                                recognizer.addGrammarConstraint(questionInformationSlotGrammar);
                                SpeakService.getInstance().speak("Okay! Follow me, I’ll show you the car: " + selectedCar.getCar().getName());
                                BaseService.getInstance().startNavigation(resetPosition, selectedCar.getSpot());
                                return true;
                            } catch (VoiceException e) {
                                Log.e(TAG, "Exception: ", e);
                            }
                            return true;
                        }
                    }
/*                    if(optionsCar.isEmpty()) {
                    }*/
                    return true;
                }

                else if (modelSelected) {
                    Log.d(TAG, "customer is asking information");
                    if (result.contains("general information") || result.contains("something about")) {
                        Log.d(TAG, "customer selected general information");
                        try {
                            modelSelected = false;
                            nextCar = true;
                            recognizer.removeGrammarConstraint(generalInformationSlotGrammar);
                            recognizer.removeGrammarConstraint(questionInformationSlotGrammar);
                            recognizer.addGrammarConstraint(yesNoSlotGrammer);
                            return true;
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        SpeakService.getInstance().speak("This is a " + selectedCar.getCar().getName()+ " As you can see, it is " +
                                selectedCar.getCar().getColor() + " and has " + selectedCar.getCar().getSeatNumber() +" seats. It can reach up to " +
                                selectedCar.getCar().getMaxSpeed() + " kilometres per hour with a horsepower that equals " +
                                selectedCar.getCar().getPower() + " The fuel type is " + selectedCar.getCar().getFuelType() +
                                " kilometres and it has a maximum fuel consumption of " + selectedCar.getCar().getMaxFuelConsumption() + " litres per 100 " +
                                "With the described equipment this car costs " + selectedCar.getCar().getPrice() + " Do you want to see another car?");
                        return true;
                    }
                    else if (result.contains("name") || result.contains("color") || result.contains("seat number") || result.contains("power") || result.contains("speed") ||
                            result.contains("transmission") || result.contains("fuel type") || result.contains("fuel consumption") || result.contains("price")) {
                        if (result.contains("name")) {
                            Log.d(TAG, "customer asked for the name");
                            SpeakService.getInstance().speak("This is the " + selectedCar.getCar().getName());
                            return true;
                        }
                        else if (result.contains("color")) {
                            Log.d(TAG, "customer asked for the color");
                            SpeakService.getInstance().speak("The car is " + selectedCar.getCar().getColor());
                            return true;
                        }
                        else if (result.contains("seat number")) {
                            Log.d(TAG, "customer asked for the seat number");
                            SpeakService.getInstance().speak("The car has " + selectedCar.getCar().getSeatNumber() + " seats.");
                            return true;
                        }
                        else if (result.contains("power")) {
                            Log.d(TAG, "customer asked for the power");
                            SpeakService.getInstance().speak("The car has a horsepower of " + selectedCar.getCar().getPower());
                            return true;
                        }
                        else if (result.contains("speed")) {
                            Log.d(TAG, "customer asked for the maximum speed");
                            SpeakService.getInstance().speak("The car can reach up to " + selectedCar.getCar().getMaxSpeed() + " kilometres per hour.");
                            return true;
                        }
                        else if (result.contains("transmission")) {
                            Log.d(TAG, "customer asked for the transmission");
                            SpeakService.getInstance().speak("The transmission is " + selectedCar.getCar().getTransmission());
                            return true;
                        }
                        else if (result.contains("fuel type")) {
                            Log.d(TAG, "customer asked for the fuel type");
                            SpeakService.getInstance().speak("The fuel type is " + selectedCar.getCar().getFuelType());
                            return true;
                        }
                        else if (result.contains("fuel consumption")) {
                            Log.d(TAG, "customer asked for the consumption");
                            SpeakService.getInstance().speak("The car has a maximum fuel consumption of " + selectedCar.getCar().getMaxFuelConsumption() + " litres per 100 kilometres.");
                            return true;
                        }
                        else if (result.contains("price")) {
                            Log.d(TAG, "customer asked for the price");
                            SpeakService.getInstance().speak("With this equipment the car costs " + selectedCar.getCar().getPrice() + " euros.");
                            return true;
                        }
                        modelSelected = false;
                        moreInformation = true;
                        try {
                            recognizer.removeGrammarConstraint(generalInformationSlotGrammar);
                            recognizer.addGrammarConstraint(yesNoSlotGrammer);
                            return true;
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        SpeakService.getInstance().speak("Is there something else you want to know?");
                        return true;
                    }
                    return true;
                }

                else if (moreInformation) {
                    if (result.contains("yes") || result.contains("yeah") || result.contains("of course") || result.contains("sure")) {
                        moreInformation = false;
                        modelSelected = true;
                        SpeakService.getInstance().speak("Okay, ask me another question.");
                        return true;
                    }
                    else if (result.contains("no") || result.contains("nah") || result.contains("nope")) {
                        moreInformation = false;
                        nextCar = true;
                        try {
                            recognizer.removeGrammarConstraint(questionInformationSlotGrammar);
                            return true;
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        SpeakService.getInstance().speak("Alright. Do you want to see another car?");
                        return true;
                    }
                    return true;
                }
                return true;
            }

            @Override
            public boolean onRecognitionError(String error) {
                Log.i(TAG, "recognition error: " + error);
                return true;
            }
        };
    }

    private void initControlGrammer() {
        Log.d(TAG, "init control grammar");

        Slot interest = new Slot( "interest ", false, Arrays.asList("Show me", "I would like to see", "Take me", "Guide me", "Can you show me"));
        Slot preposition = new Slot("preposition", true, Arrays.asList("to", "a"));
        Slot answerPos = new Slot("answer positive", false, Arrays.asList("yes", "yeah", "sure", "of course", "yes please"));
        Slot answerNeg = new Slot("answer negative", false, Arrays.asList("no", "nah", "nope", "no thanks"));
        Slot article = new Slot("article", true, Arrays.asList("the", "this", "that"));
        Slot modelName = new Slot("model name", false, Arrays.asList("car", "model", "A-Class", "B-Class", "C-Class", "CLA", "CLS", "S-Class",
                "E-Class", "G-Class", "GLA", "GLC","GLE","V-Class"));

        yesNoSlotGrammer = new GrammarConstraint();
        yesNoSlotGrammer.setName("yes or no");
        yesNoSlotGrammer.addSlot(answerPos);
        yesNoSlotGrammer.addSlot(answerNeg);

        categorySlotGrammar = new GrammarConstraint();
        categorySlotGrammar.setName("category");
        categorySlotGrammar.addSlot(interest);
        categorySlotGrammar.addSlot(preposition);
        categorySlotGrammar.addSlot(new Slot("category", false, Arrays.asList("Hatchback", "Coupé", "Saloon", "Cabriolet", "SUV", "MPV" )));

        modelSlotGrammar = new GrammarConstraint();
        modelSlotGrammar.setName("model");
        modelSlotGrammar.addSlot(interest);
        modelSlotGrammar.addSlot(preposition);
        modelSlotGrammar.addSlot(modelName);

        generalInformationSlotGrammar = new GrammarConstraint();
        generalInformationSlotGrammar.setName("general information");
        generalInformationSlotGrammar.addSlot(new Slot("command", false, Arrays.asList("Tell me general information about", "Tell me something about")));
        generalInformationSlotGrammar.addSlot(article);
        generalInformationSlotGrammar.addSlot(modelName);

        questionInformationSlotGrammar = new GrammarConstraint();
        questionInformationSlotGrammar.setName("question information");
        questionInformationSlotGrammar.addSlot(new Slot("question", false, Arrays.asList("What is the")));
        questionInformationSlotGrammar.addSlot(new Slot("information type", false, Arrays.asList("name of", "color of",
                "seat number of", "power of", "maximum speed of", "transmission of", "fuel type of", "maximum fuel consupmtion of", "price of" )));
        questionInformationSlotGrammar.addSlot(new Slot("car", false, Arrays.asList("this car", "that car")));

        additionalConsultationSlotGrammar = new GrammarConstraint();
        additionalConsultationSlotGrammar.setName("additional consultation");
        additionalConsultationSlotGrammar.addSlot(new Slot("wanting", false, Arrays.asList("I want to", "I would like to")));
        additionalConsultationSlotGrammar.addSlot(new Slot("verb", false, Arrays.asList("receive", "do", "have", "make")));
        additionalConsultationSlotGrammar.addSlot(new Slot("consultation", false, Arrays.asList("an offer", "a phone call", "a test drive")));
    }

    public void startListening() {
        Log.d(TAG, "start listening");
        SpeakService.getInstance().speak("Hello, I am Loomo, the Car Master. Do you want to know something about our cars?");
        try {
            recognizer.addGrammarConstraint(yesNoSlotGrammer);
            recognizer.startRecognitionMode(recognitionListener);
        }
        catch (VoiceException e) {
            Log.w(TAG, "Exception: ", e);
        }
    }

    private void filterMapObjectsByCategory(Category cat) {
        for (MapObject obj : mapObjects) {
            if(obj.getCar().getCategory().getName() == cat.getName()) {
                optionsCar.add(obj);
            }
        }
    }

    private void filterMapObjectsByCarModel(CarModel model) {
        for (MapObject obj : optionsCar) {
            if(obj.getCar().getCarModel().getName() != model.getName()) {
                optionsCar.remove(obj);
            }
        }
    }

    public void stopListening() {
        Log.d(TAG, "stop listening");
        try {
            recognizer.stopRecognition();
        } catch (VoiceException e) {
            Log.e(TAG, "got VoiceException", e);
        }
    }

    public void disconnect() {
        Log.d(TAG, "unbind recognizer service");
        recognizer.unbindService();
    }
}

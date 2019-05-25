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
import java.util.List;

public class RecognitionService extends Service {
    private static final String TAG = "RecognitionService";
    private final Context context;

    private Recognizer recognizer;
    private static RecognitionService instance;

    private RecognitionListener recognitionListener;

    private static List<String> yesCommandList;
    private static List<String> noCommandList;

    private GrammarConstraint yesSlotGrammar;
    private GrammarConstraint noSlotGrammar;
    private GrammarConstraint categorySlotGrammar;
    private GrammarConstraint modelSlotGrammar;
    private GrammarConstraint generalInformationSlotGrammar;
    private GrammarConstraint questionInformationSlotGrammar;
    private GrammarConstraint additionalConsultationSlotGrammar;

    private boolean resetPosition = true;
    private String dialogueStatus = "";

    private ArrayList<Category> categories;
    private ArrayList<CarModel> carModels;
    private ArrayList<MapObject> mapObjects;

    private ArrayList<MapObject> carOptions = new ArrayList<>();
    private MapObject selectedMapObject;

    /**
     * return the recognizer instance
     * @return RecognitionService
     */
    public static RecognitionService getInstance() {
        Log.d(TAG, "get recognizer instance");
        if (instance == null) {
            throw new IllegalStateException("RecognitionService instance not initialized yet");
        }
        return instance;
    }

    /**
     * constructor to initialize the recognition service
     * @param context
     */
    public RecognitionService(Context context) {
        Log.d(TAG, "recognition service initiated");
        this.context = context;
        this.init();
        this.initListeners();
        instance = this;
    }

    /**
     * initialize the recognizer instance
     */
    @Override
    public void init() {
        recognizer = Recognizer.getInstance();
        recognizer.bindService(this.context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "recognizer service bound successfully");
                initControlGrammar();
            }

            @Override
            public void onUnbind(String reason) {
                Log.d(TAG, "recognizer service unbound");
            }
        });
    }

    /**
     * initialize the recognition listener
     */
    @Override
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

                // handle dialogue start
                if (dialogueStatus.equals(DialogueStatus.START_DIALOGUE)) {
                    if (isCommand(result, yesCommandList)) {
                        Log.d(TAG, "customer is interested");

                        // get categories from database
                        categories = (ArrayList<Category>) RequestHandler.getInstance().makeRequest(RequestHandler.Collection.CATEGORIES);
                        /*carModels = (ArrayList<CarModel>) RequestHandler.getInstance().makeRequest(RequestHandler.Collection.CAR_MODELS);*/

                        // get map objects from database which include the car and its respective spot
                        mapObjects = (ArrayList<MapObject>) RequestHandler.getInstance().makeRequest(RequestHandler.Collection.SHOWROOM_MAP);

                        try {
                            recognizer.removeGrammarConstraint(yesSlotGrammar);
                            recognizer.removeGrammarConstraint(noSlotGrammar);
                            recognizer.addGrammarConstraint(categorySlotGrammar);
                            SpeakService.getInstance().speak("What category are you interested in?");

                            // tell customer each available category
                            for (Category cat : categories) {
                                SpeakService.getInstance().speak(cat.getName());
                            }
                            dialogueStatus = DialogueStatus.CUSTOMER_INTERESTED;
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        return true;
                    }

                    else if (isCommand(result, noCommandList)) {
                        Log.d(TAG, "customer not interested");
                        try {
                            recognizer.removeGrammarConstraint(yesSlotGrammar);
                            recognizer.removeGrammarConstraint(noSlotGrammar);

                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        stopListening();
                        SpeakService.getInstance().speak("Alright. Thank you and have a nice day!");
                        return false;
                    }
                    return true;
                }

                // handle category selection
                else if (dialogueStatus.equals(DialogueStatus.CUSTOMER_INTERESTED)) {
                    Log.d(TAG, "customer is selecting category");
                    boolean categoryFound = false;
                    // loop through the categories to check which category the customer selected
                    for (Category cat : categories) {
                        if (result.toLowerCase().contains(cat.getName().toLowerCase())) {
                            Log.d(TAG, cat.getName() + " is selected");
                            categoryFound = true;

                            // filter map objects by selected category to have the car options for the customer
                            carOptions = filterMapObjectsByCategory(cat);

                            // get the available car models of the car options
                            carModels = getCarModelsOfCarOptions();
                            try {
                                recognizer.removeGrammarConstraint(categorySlotGrammar);
                                recognizer.addGrammarConstraint(modelSlotGrammar);
                                SpeakService.getInstance().speak("Alright. Please tell me which model I should show you. Available models for this category are: ");

                                // tell customer each available car model for the selected category
                                for (CarModel model : carModels) {
                                    SpeakService.getInstance().speak(model.getName());
                                }
                                dialogueStatus = DialogueStatus.CATEGORY_SELECTED;
                            } catch (VoiceException e) {
                                Log.e(TAG, "Exception: ", e);
                            }
                            return true;
                        }
                    }
                    if (!categoryFound) {
                        Log.d(TAG, "category not found");
                        return true;
                    }
                    return true;
                }

                // handle car model selection
                else if (dialogueStatus.equals(DialogueStatus.CATEGORY_SELECTED)) {
                    Log.d(TAG, "customer is selecting car model");
                    boolean modelFound = false;

                    // loop through the car models of the car options to check which car model the customer selected
                    for (MapObject obj : carOptions) {
                        CarModel model = obj.getCar().getCarModel();
                        if (result.toLowerCase().contains(model.getName().toLowerCase())) {
                            Log.d(TAG, model.getName() + " is selected");
                            modelFound = true;

                            /*filterMapObjectsByCarModel(model);*/
                            try {
                                selectedMapObject = obj;
                                recognizer.removeGrammarConstraint(modelSlotGrammar);
                                recognizer.addGrammarConstraint(generalInformationSlotGrammar);
                                recognizer.addGrammarConstraint(questionInformationSlotGrammar);
                                SpeakService.getInstance().speak("Okay! Follow me, I’ll show you the car: " + selectedMapObject.getCar().getName());

                                // guide customer to the selected car
                                BaseService.getInstance().startNavigation(resetPosition, selectedMapObject.getSpot());

                                // set reset position to false because we only want to reset the position once, namely at the beginning
                                resetPosition = false;

                                dialogueStatus = DialogueStatus.MODEL_SELECTED;
                            } catch (VoiceException e) {
                                Log.e(TAG, "Exception: ", e);
                            }
                            return true;
                        }
                    }
                    if (!modelFound) {
                        Log.d(TAG, "model not found");
                        return true;
                    }
                    return true;
                }

                // handle questions regarding car information
                else if (dialogueStatus.equals(DialogueStatus.MODEL_SELECTED)) {
                    Log.d(TAG, "customer is asking information");

                    if (result.contains("general information") || result.contains("something about")) {
                        Log.d(TAG, "customer selected general information");
                        try {
                            recognizer.removeGrammarConstraint(generalInformationSlotGrammar);
                            recognizer.removeGrammarConstraint(questionInformationSlotGrammar);
                            recognizer.addGrammarConstraint(yesSlotGrammar);
                            recognizer.addGrammarConstraint(noSlotGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        SpeakService.getInstance().speak("This is a " + selectedMapObject.getCar().getName()+ ". As you can see, it is " +
                                selectedMapObject.getCar().getColor() + " and has " + selectedMapObject.getCar().getSeatNumber() + " seats. It can reach up to " +
                                selectedMapObject.getCar().getMaxSpeed() + " kilometres per hour with a horsepower that equals " +
                                selectedMapObject.getCar().getPower() + ". The fuel type is " + selectedMapObject.getCar().getFuelType() +
                                " and it has a maximum fuel consumption of " + selectedMapObject.getCar().getMaxFuelConsumption() + " litres per 100 kilometres. " +
                                "With the described equipment this car costs " + selectedMapObject.getCar().getPrice() + " euros. Do you want to see another car?");
                        dialogueStatus = DialogueStatus.NEXT_CAR;
                        return true;
                    }

                    else if (result.contains("name") || result.contains("color") || result.contains("seat number") || result.contains("power") || result.contains("speed") ||
                            result.contains("transmission") || result.contains("fuel type") || result.contains("fuel consumption") || result.contains("price")) {
                        if (result.contains("name")) {
                            Log.d(TAG, "customer asked for the name");
                            SpeakService.getInstance().speak("This is the " + selectedMapObject.getCar().getName());
                        }
                        else if (result.contains("color")) {
                            Log.d(TAG, "customer asked for the color");
                            SpeakService.getInstance().speak("The car is " + selectedMapObject.getCar().getColor());
                        }
                        else if (result.contains("seat number")) {
                            Log.d(TAG, "customer asked for the seat number");
                            SpeakService.getInstance().speak("The car has " + selectedMapObject.getCar().getSeatNumber() + " seats.");
                        }
                        else if (result.contains("power")) {
                            Log.d(TAG, "customer asked for the power");
                            SpeakService.getInstance().speak("The car has a horsepower of " + selectedMapObject.getCar().getPower());
                        }
                        else if (result.contains("speed")) {
                            Log.d(TAG, "customer asked for the maximum speed");
                            SpeakService.getInstance().speak("The car can reach up to " + selectedMapObject.getCar().getMaxSpeed() + " kilometres per hour.");
                        }
                        else if (result.contains("transmission")) {
                            Log.d(TAG, "customer asked for the transmission");
                            SpeakService.getInstance().speak("The transmission is " + selectedMapObject.getCar().getTransmission());
                        }
                        else if (result.contains("fuel type")) {
                            Log.d(TAG, "customer asked for the fuel type");
                            SpeakService.getInstance().speak("The fuel type is " + selectedMapObject.getCar().getFuelType());
                        }
                        else if (result.contains("fuel consumption")) {
                            Log.d(TAG, "customer asked for the consumption");
                            SpeakService.getInstance().speak("The car has a maximum fuel consumption of " + selectedMapObject.getCar().getMaxFuelConsumption() + " litres per 100 kilometres.");
                        }
                        else if (result.contains("price")) {
                            Log.d(TAG, "customer asked for the price");
                            SpeakService.getInstance().speak("With this equipment the car costs " + selectedMapObject.getCar().getPrice() + " euros.");
                        }
                        try {
                            recognizer.removeGrammarConstraint(questionInformationSlotGrammar);
                            recognizer.addGrammarConstraint(yesSlotGrammar);
                            recognizer.addGrammarConstraint(noSlotGrammar);
                            recognizer.removeGrammarConstraint(generalInformationSlotGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        SpeakService.getInstance().speak("Is there something else you want to know?");
                        dialogueStatus = DialogueStatus.MORE_INFORMATION;
                        return true;
                    }
                    return true;
                }

                // handle if customer wants to receive more information
                else if (dialogueStatus.equals(DialogueStatus.MORE_INFORMATION)) {

                    if (isCommand(result, noCommandList)) {
                        Log.d(TAG, "customer does not want to receive more information");
                        SpeakService.getInstance().speak("Alright. Do you want to see another car?");
                        dialogueStatus = DialogueStatus.NEXT_CAR;
                        return true;
                    }

                    else if (isCommand(result, yesCommandList)) {
                        Log.d(TAG, "customer wants to receive more information");
                        try {
                            recognizer.removeGrammarConstraint(yesSlotGrammar);
                            recognizer.removeGrammarConstraint(noSlotGrammar);
                            recognizer.addGrammarConstraint(questionInformationSlotGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        SpeakService.getInstance().speak("Okay, ask me another question.");
                        dialogueStatus = DialogueStatus.MODEL_SELECTED;
                        return true;
                    }
                    return true;
                }

                // handle if customer wants to see another car
                else if (dialogueStatus.equals(DialogueStatus.NEXT_CAR)) {

                    if (isCommand(result, noCommandList)) {
                        Log.d(TAG, "customer does not want to see another car");
                        SpeakService.getInstance().speak("Alright. If you have more questions, you could talk to a human salesman now. Should I call someone?");
                        dialogueStatus = DialogueStatus.CALL_SALESMAN;
                        return true;
                    }

                    else if (isCommand(result, yesCommandList)) {
                        Log.d(TAG, "customer wants to see another car");
                        try {
                            recognizer.removeGrammarConstraint(yesSlotGrammar);
                            recognizer.removeGrammarConstraint(noSlotGrammar);
                            recognizer.addGrammarConstraint(categorySlotGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        SpeakService.getInstance().speak("What category are you interested in?");

                        // tell customer each available category
                        for (Category cat : categories) {
                            SpeakService.getInstance().speak(cat.getName());
                        }
                        dialogueStatus = DialogueStatus.CUSTOMER_INTERESTED;
                        return true;
                    }
                    return true;
                }

                // handle if customer wants to see call a salesman
                else if (dialogueStatus.equals(DialogueStatus.CALL_SALESMAN)) {

                    if (isCommand(result, noCommandList)) {
                        Log.d(TAG, "customer does not want to call a salesman");
                        try {
                            recognizer.removeGrammarConstraint(yesSlotGrammar);
                            recognizer.addGrammarConstraint(additionalConsultationSlotGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        SpeakService.getInstance().speak("Alright, Do you want to get additional consultation? We could offer you a phone call," +
                                        " an appointment for a test drive or a sales offer.");
                        dialogueStatus = DialogueStatus.ADDITIONAL_CONSULTATION;
                        return true;
                    }

                    else if (isCommand(result, yesCommandList)) {
                        Log.d(TAG, "customer wants to call a salesman");
                        try {
                            recognizer.removeGrammarConstraint(yesSlotGrammar);
                            recognizer.removeGrammarConstraint(noSlotGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        stopListening();
                        SpeakService.getInstance().speak("Okay, I will call a salesman. Please wait here, it will only take some minutes.");
                        //cms hook --> request url to trigger a web hook
                        return false;
                    }

                    return true;
                }

                // handle if customer wants additional consultation like sales offer, phone call or test drive
                else if (dialogueStatus.equals(DialogueStatus.ADDITIONAL_CONSULTATION)) {

                    if (isCommand(result, noCommandList)) {
                        Log.d(TAG, "customer does not want to additional consultation");
                        try {
                            recognizer.removeGrammarConstraint(additionalConsultationSlotGrammar);
                            recognizer.removeGrammarConstraint(noSlotGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        stopListening();
                        SpeakService.getInstance().speak("Okay. Thank you! See you next time!");
                        return false;
                    }

                    else if (result.contains("offer") || result.contains("phone call") || result.contains("test drive")) {
                        Log.d(TAG, "customer wants additional consultation");
                        if (result.contains("offer")) {
                            Log.d(TAG, "customer wants a sales offer");
                            //write offer into database
                        }
                        else if (result.contains("phone call")) {
                            Log.d(TAG, "customer wants a phone call");
                            //write phone call into database
                        }
                        else if (result.contains("test drive")) {
                            Log.d(TAG, "customer wants a test drive");
                            //write test drive into database
                        }
                        try {
                            recognizer.removeGrammarConstraint(additionalConsultationSlotGrammar);
                            recognizer.addGrammarConstraint(yesSlotGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        SpeakService.getInstance().speak("We need to collect some information from you to make an appointment or" +
                                "to send you an offer. Are you okay with that?");
                        dialogueStatus = DialogueStatus.CONTACT_INFORMATION;
                        return true;
                    }
                    return true;
                }

                // handle contact information input
                else if (dialogueStatus.equals(DialogueStatus.CONTACT_INFORMATION)) {
                    boolean shouldRemove = false;
                    if (isCommand(result, yesCommandList)) {
                        shouldRemove = true;
                        SpeakService.getInstance().speak("Please enter your contact information on the screen and confirm.");
                        //show contact form --> on send button pressed: SpeakService.getInstance().speak("Thank you! We will contact you as soon as possible. Goodbye and have a nice day!");
                    }
                    else if (isCommand(result, noCommandList)) {
                        shouldRemove = true;
                        SpeakService.getInstance().speak("Okay. We are sorry that we cannot offer you additional consultation without your contact information. Thank you and see you next time!");
                    }
                    if (shouldRemove) {
                        try {
                            recognizer.removeGrammarConstraint(yesSlotGrammar);
                            recognizer.removeGrammarConstraint(noSlotGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        stopListening();
                        return false;
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

    /**
     *  initialize the control grammar so that loomo can recognize specific sentences with a specific grammar
     */
    private void initControlGrammar() {
        Log.d(TAG, "init control grammar");

        Slot interest = new Slot( "interest ", false, Arrays.asList("show me", "I would like to see", "take me", "guide me", "can you show me",
                "I want to see", "I am interested in"));
        Slot preposition = new Slot("preposition", true, Arrays.asList("to"));
        Slot article = new Slot("article", true, Arrays.asList("the", "this", "that", "a"));
        Slot modelName = new Slot("model name", false, Arrays.asList("car", "model", "A class", "B class", "C class", "CLA", "CLS", "S class",
                "E class", "G class", "GLA", "GLC","GLE","V class"));

        yesCommandList = Arrays.asList("yes", "yeah", "sure", "of course", "yes please");
        noCommandList = Arrays.asList("no", "nah", "nope", "no thanks");

        yesSlotGrammar = new GrammarConstraint();
        yesSlotGrammar.setName("yes");
        yesSlotGrammar.addSlot(new Slot("answer positive", false, yesCommandList));

        noSlotGrammar = new GrammarConstraint();
        noSlotGrammar.setName("no");
        noSlotGrammar.addSlot(new Slot("answer negative", false, noCommandList));

        categorySlotGrammar = new GrammarConstraint();
        categorySlotGrammar.setName("category");
        categorySlotGrammar.addSlot(interest);
        categorySlotGrammar.addSlot(article);
        categorySlotGrammar.addSlot(new Slot("category", false, Arrays.asList("hatchback", "coupé", "saloon", "cabriolet", "SUV", "MPV" )));

        modelSlotGrammar = new GrammarConstraint();
        modelSlotGrammar.setName("model");
        modelSlotGrammar.addSlot(interest);
        modelSlotGrammar.addSlot(preposition);
        modelSlotGrammar.addSlot(article);
        modelSlotGrammar.addSlot(modelName);

        generalInformationSlotGrammar = new GrammarConstraint();
        generalInformationSlotGrammar.setName("general information");
        generalInformationSlotGrammar.addSlot(new Slot("command", false, Arrays.asList("tell me general information about", "tell me something about")));
        generalInformationSlotGrammar.addSlot(article);
        generalInformationSlotGrammar.addSlot(modelName);

        questionInformationSlotGrammar = new GrammarConstraint();
        questionInformationSlotGrammar.setName("question information");
        questionInformationSlotGrammar.addSlot(new Slot("question", false, Arrays.asList("what is the")));
        questionInformationSlotGrammar.addSlot(new Slot("information type", false, Arrays.asList("name of", "color of",
                "seat number of", "power of", "maximum speed of", "transmission of", "fuel type of", "maximum fuel consupmtion of", "price of" )));
        questionInformationSlotGrammar.addSlot(article);
        questionInformationSlotGrammar.addSlot(modelName);

        additionalConsultationSlotGrammar = new GrammarConstraint();
        additionalConsultationSlotGrammar.setName("additional consultation");
        additionalConsultationSlotGrammar.addSlot(new Slot("wanting", false, Arrays.asList("I want to", "I would like to")));
        additionalConsultationSlotGrammar.addSlot(new Slot("verb", false, Arrays.asList("receive", "do")));
        additionalConsultationSlotGrammar.addSlot(new Slot("consultation", false, Arrays.asList("an offer", "a phone call", "a test drive")));
    }

    /**
     *  start the recognition
     */
    public void startListening() {
        Log.d(TAG, "start listening");
        this.dialogueStatus = DialogueStatus.START_DIALOGUE;
        try {
            recognizer.addGrammarConstraint(yesSlotGrammar);
            recognizer.addGrammarConstraint(noSlotGrammar);
            recognizer.startRecognitionMode(recognitionListener);
        }
        catch (VoiceException e) {
            Log.w(TAG, "Exception: ", e);
        }
    }

    /**
     * check if the the recognition result contains a string of the given command list
     * @param recognition
     * @param commandList
     * @return true if the result is a command, false if not
     */
    private static boolean isCommand(String recognition, List<String> commandList) {
        for (String value : commandList) {
            if (recognition.toLowerCase().contains(value.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * filter the map objects by the given category and add them to the optionsCar arraylist
     * @param cat
     * @return filtered list of cars which represent the options of the customer
     */
    private ArrayList<MapObject> filterMapObjectsByCategory(Category cat) {
        ArrayList<MapObject> carObjects = new ArrayList<>();
        for (MapObject obj : mapObjects) {
            if(obj.getCar().getCategory().getName().equals(cat.getName())) {
                carObjects.add(obj);
            }
        }
        return carObjects;
    }

    /**
     * get the distinct car models of the car options
     * @return available car models of the filtered car options
     */
    private ArrayList<CarModel> getCarModelsOfCarOptions() {
        ArrayList<CarModel> models = new ArrayList<>();
        for (MapObject obj : carOptions) {
            CarModel objCarModel = obj.getCar().getCarModel();
            boolean shouldPush = true;
            if (!models.isEmpty()) {
                for (CarModel model : models) {
                    if (objCarModel.getName().equals(model.getName())) {
                        shouldPush = false;
                    }
                }
            }
            if (shouldPush) {
                models.add(objCarModel);
            }
        }
        return models;
    }

    /*private void filterMapObjectsByCarModel(CarModel model) {
        for (MapObject obj : optionsCar) {
            if(obj.getCar().getCarModel().getName() != model.getName()) {
                optionsCar.remove(obj);
            }
        }
    }*/

    /**
     *  end the recognition
     */
    public void stopListening() {
        Log.d(TAG, "stop listening");
        dialogueStatus = "";
        try {
            recognizer.stopRecognition();
        } catch (VoiceException e) {
            Log.e(TAG, "got VoiceException", e);
        }
    }

    /**
     *  disconnect the recognizer service
     */
    public void disconnect() {
        Log.d(TAG, "unbind recognizer service");
        recognizer.unbindService();
    }

    /**
     *  class which defines the dialogue status possibilities
     */
    private class DialogueStatus {
        public static final String START_DIALOGUE = "dialog start";
        public static final String CUSTOMER_INTERESTED = "customer interested";
        public static final String CATEGORY_SELECTED = "category selected";
        public static final String MODEL_SELECTED = "model selected";
        public static final String NEXT_CAR = "next car";
        public static final String MORE_INFORMATION = "more information";
        public static final String CALL_SALESMAN = "call salesman";
        public static final String ADDITIONAL_CONSULTATION = "additional consultation";
        public static final String CONTACT_INFORMATION = "contact information";
    }
}

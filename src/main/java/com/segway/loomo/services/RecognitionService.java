package com.segway.loomo.services;

import android.content.Context;
import android.util.Log;

import com.segway.loomo.MainActivity;
import com.segway.loomo.objects.CarModel;
import com.segway.loomo.objects.Category;
import com.segway.loomo.objects.Customer;
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

/**
 * class to provide the recognition service and handle recognition results
 */
public class RecognitionService extends Service {
    private static final String TAG = "RecognitionService";

    /**
     * the application context
     */
    private Context context;

    /**
     * recognizer instance
     */
    private Recognizer recognizer;

    /**
     * recognition service instance
     */
    private static RecognitionService instance;

    /**
     * recognition listener
     */
    private RecognitionListener recognitionListener;

    /**
     * grammar constraints for speech recognition
     */
    private GrammarConstraint yesNoGrammar;
    private GrammarConstraint categorySelectionGrammar;
    private GrammarConstraint modelSelectionGrammar;
    private GrammarConstraint questionInformationGrammar;
    private GrammarConstraint additionalConsultationGrammar;

    /**
     * yes and no command list
     */
    private List<String> yesCommandList;
    private List<String> noCommandList;

    /**
     * dialogue status to track the status of or position at the dialogue flow
     */
    private DialogueStatus dialogueStatus = DialogueStatus.BEGINNING;

    /**
     * arraylist to hold the mapobjects of the map, including the available cars with their respective spot position
     */
    private ArrayList<MapObject> carOptions;

    /**
     * arraylist to hold the available car models of the cars in the car showroom
     */
    private ArrayList<CarModel> carModelOptions;

    /**
     * selected map object to navigate to and to show the customer
     */
    private MapObject selectedMapObject;

    /**
     * returns the recognizer instance
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
        instance = this;
        this.init();
        this.initListeners();

        this.yesCommandList = Arrays.asList("yes", "yeah", "sure", "of course", "yes please");
        this.noCommandList = Arrays.asList("no", "nah", "nope", "no thanks");
    }

    /**
     * initialize the recognizer instance
     */
    @Override
    public void init() {
        Log.d(TAG, "init method");
        this.recognizer = Recognizer.getInstance();
        this.recognizer.bindService(this.context, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Log.d(TAG, "recognizer service bound successfully");
                RecognitionService.getInstance().initControlGrammar();
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
        Log.i(TAG, "init listeners");

        this.recognitionListener = new RecognitionListener() {
            @Override
            public void onRecognitionStart() {
                Log.i(TAG, "recognition started");
            }

            @Override
            public boolean onRecognitionResult(RecognitionResult recognitionResult) {
                //log the recognition result and recognition result confidence.
                Log.d(TAG, "recognition result: " + recognitionResult.getRecognitionResult() +
                        ", confidence:" + recognitionResult.getConfidence() + ", dialog status: " + dialogueStatus);
                String result = recognitionResult.getRecognitionResult();

                // handle dialogue start
                if (dialogueStatus.equals(DialogueStatus.START_DIALOGUE)) {

                    //handle if customer is interested and wants to see the cars in the car showroom
                    if (isCommand(result, yesCommandList)) {
                        Log.d(TAG, "customer is interested");
                        try {
                            recognizer.removeGrammarConstraint(yesNoGrammar);
                            recognizer.addGrammarConstraint(categorySelectionGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }

                        //prepare sentence to tell customer the available categories
                        String categoryQuestion = buildCategoryString();
                        //MainActivity.getInstance().changeInfoText(categoryQuestion);
                        SpeakService.getInstance().speak(categoryQuestion);

                        dialogueStatus = DialogueStatus.CUSTOMER_INTERESTED;
                        return true;
                    }

                    // handle if customer is not interested and does not want to see the cars in the car showroom
                    else if (isCommand(result, noCommandList)) {
                        Log.d(TAG, "customer not interested");
                        try {
                            recognizer.removeGrammarConstraint(yesNoGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        RecognitionService.getInstance().stopListening();
                        String text = "Alright. Thank you and have a nice day!";
                        //MainActivity.getInstance().changeInfoText(text);
                        SpeakService.getInstance().speak(text);
                        MainActivity.getInstance().restart(false);
                        return false;
                    }
                    return true;
                }

                // handle category selection
                else if (dialogueStatus.equals(DialogueStatus.CUSTOMER_INTERESTED)) {
                    Log.d(TAG, "customer is selecting category");
                    boolean categoryFound = false;
                    // loop through the categories to check which category the customer selected
                    for (Category cat : MainActivity.getInstance().categories) {
                        if (result.toLowerCase().contains(cat.getName().toLowerCase())) {
                            Log.d(TAG, cat.getName() + " is selected");
                            categoryFound = true;

                            // filter map objects by selected category to have the car options for the customer
                            carOptions = RecognitionService.getInstance().filterMapObjectsByCategory(cat);

                            // get the available car models of the car options
                            carModelOptions = RecognitionService.getInstance().getCarModelsOfCarOptions();

                            try {
                                recognizer.removeGrammarConstraint(categorySelectionGrammar);
                                recognizer.addGrammarConstraint(modelSelectionGrammar);
                            } catch (VoiceException e) {
                                Log.e(TAG, "Exception: ", e);
                            }

                            //prepare sentence to tell customer the available car models
                            String modelQuestion = buildModelString();
                            //MainActivity.getInstance().changeInfoText(modelQuestion);
                            SpeakService.getInstance().speak(modelQuestion);

                            dialogueStatus = DialogueStatus.CATEGORY_SELECTED;
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
                            try {
                                modelFound = true;

                                selectedMapObject = obj;
                                recognizer.removeGrammarConstraint(modelSelectionGrammar);
                                recognizer.addGrammarConstraint(questionInformationGrammar);
                            } catch (VoiceException e) {
                                Log.e(TAG, "Exception: ", e);
                            }
                            String text = "Okay! Follow me, I’ll show you the car: " + selectedMapObject.getCar().getName();
                            //MainActivity.getInstance().changeInfoText(text);
                            SpeakService.getInstance().speak(text);

                            // guide customer to the selected car
                            BaseService.getInstance().startNavigation(selectedMapObject.getSpot());

                            dialogueStatus = DialogueStatus.MODEL_SELECTED;
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

                    // handle if customer wants to receive general information about the car
                    if (result.contains("general information") || result.contains("something about")) {
                        Log.d(TAG, "customer selected general information");
                        try {
                            recognizer.removeGrammarConstraint(questionInformationGrammar);
                            recognizer.addGrammarConstraint(yesNoGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        String text = "This is a " + selectedMapObject.getCar().getName()+ ". As you can see, it is " +
                                selectedMapObject.getCar().getColor() + " and has " + selectedMapObject.getCar().getSeatNumber() + " seats. It can reach up to " +
                                selectedMapObject.getCar().getMaxSpeed() + " kilometres per hour and has " + selectedMapObject.getCar().getPower() +
                                " horsepower. The fuel type is " + selectedMapObject.getCar().getFuelType() + " and it has a maximum fuel consumption of " +
                                selectedMapObject.getCar().getMaxFuelConsumption() + " litres per 100 kilometres. " + "With the described equipment this car costs " +
                                selectedMapObject.getCar().getPrice() + " euros.";
                        //MainActivity.getInstance().changeInfoText(text);
                        SpeakService.getInstance().speak(text);
                        SpeakService.getInstance().speak("Do you want to see another car?");
                        dialogueStatus = DialogueStatus.NEXT_CAR;
                        return true;
                    }

                    // handle if customer wants to receive specific information about the car
                    else if (result.contains("name") || result.contains("color") || result.contains("seat number") || result.contains("power") || result.contains("speed") ||
                            result.contains("transmission") || result.contains("fuel type") || result.contains("fuel consumption") || result.contains("price")) {
                        try {
                            recognizer.removeGrammarConstraint(questionInformationGrammar);
                            recognizer.addGrammarConstraint(yesNoGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        if (result.contains("name")) {
                            Log.d(TAG, "customer asked for the name");
                            String text = "This is the " + selectedMapObject.getCar().getName();
                            //MainActivity.getInstance().changeInfoText(text);
                            SpeakService.getInstance().speak(text);
                        }
                        else if (result.contains("color")) {
                            Log.d(TAG, "customer asked for the color");
                            String text = "The car is " + selectedMapObject.getCar().getColor();
                            //MainActivity.getInstance().changeInfoText(text);
                            SpeakService.getInstance().speak(text);
                        }
                        else if (result.contains("seat number")) {
                            Log.d(TAG, "customer asked for the seat number");
                            String text = "The car has " + selectedMapObject.getCar().getSeatNumber() + " seats.";
                            //MainActivity.getInstance().changeInfoText(text);
                            SpeakService.getInstance().speak(text);
                        }
                        else if (result.contains("power")) {
                            Log.d(TAG, "customer asked for the power");
                            String text = "The car has a horsepower of " + selectedMapObject.getCar().getPower();
                            //MainActivity.getInstance().changeInfoText(text);
                            SpeakService.getInstance().speak(text);
                        }
                        else if (result.contains("speed")) {
                            Log.d(TAG, "customer asked for the maximum speed");
                            String text = "The car can reach up to " + selectedMapObject.getCar().getMaxSpeed() + " kilometres per hour.";
                            //MainActivity.getInstance().changeInfoText(text);
                            SpeakService.getInstance().speak(text);
                        }
                        else if (result.contains("transmission")) {
                            Log.d(TAG, "customer asked for the transmission");
                            String text = "The transmission is " + selectedMapObject.getCar().getTransmission();
                            //MainActivity.getInstance().changeInfoText(text);
                            SpeakService.getInstance().speak(text);
                        }
                        else if (result.contains("fuel type")) {
                            Log.d(TAG, "customer asked for the fuel type");
                            String text = "The fuel type is " + selectedMapObject.getCar().getFuelType();
                            //MainActivity.getInstance().changeInfoText(text);
                            SpeakService.getInstance().speak(text);
                        }
                        else if (result.contains("fuel consumption")) {
                            Log.d(TAG, "customer asked for the consumption");
                            String text = "The car has a maximum fuel consumption of " + selectedMapObject.getCar().getMaxFuelConsumption() + " litres per 100 kilometres.";
                            //MainActivity.getInstance().changeInfoText(text);
                            SpeakService.getInstance().speak(text);
                        }
                        else if (result.contains("price")) {
                            Log.d(TAG, "customer asked for the price");
                            String text = "With this equipment the car costs " + selectedMapObject.getCar().getPrice() + " euros.";
                            //MainActivity.getInstance().changeInfoText(text);
                            SpeakService.getInstance().speak(text);
                        }
                        Log.d(TAG, "ask whether customer has more questions");
                        String text = "Is there something else you want to know?";
                        //MainActivity.getInstance().changeInfoText(text);
                        SpeakService.getInstance().speak(text);
                        dialogueStatus = DialogueStatus.MORE_INFORMATION;
                        return true;
                    }
                    return true;
                }

                // handle if customer wants to receive more information
                else if (dialogueStatus.equals(DialogueStatus.MORE_INFORMATION)) {

                    // handle if customer does not want to receive more information about the car
                    if (isCommand(result, noCommandList)) {
                        Log.d(TAG, "customer does not want to receive more information");
                        try {
                            recognizer.removeGrammarConstraint(questionInformationGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        String text = "Alright. Do you want to see another car?";
                        //MainActivity.getInstance().changeInfoText(text);
                        SpeakService.getInstance().speak(text);
                        dialogueStatus = DialogueStatus.NEXT_CAR;
                        return true;
                    }

                    // handle if customer wants to receive more information about the car
                    else if (isCommand(result, yesCommandList)) {
                        Log.d(TAG, "customer wants to receive more information");
                        try {
                            recognizer.removeGrammarConstraint(yesNoGrammar);
                            recognizer.addGrammarConstraint(questionInformationGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        String text = "Okay, ask me another question.";
                        //MainActivity.getInstance().changeInfoText(text);
                        SpeakService.getInstance().speak(text);
                        dialogueStatus = DialogueStatus.MODEL_SELECTED;
                        return true;
                    }
                    return true;
                }

                // handle if customer wants to see another car
                else if (dialogueStatus.equals(DialogueStatus.NEXT_CAR)) {

                    // handle if customer does not want to see another car
                    if (isCommand(result, noCommandList)) {
                        Log.d(TAG, "customer does not want to see another car");
                        String text = "Okay. If you have more questions, you could talk to a human salesman now. Should I call someone?";
                        //MainActivity.getInstance().changeInfoText(text);
                        SpeakService.getInstance().speak(text);
                        dialogueStatus = DialogueStatus.CALL_SALESMAN;
                        return true;
                    }

                    // handle if customer wants to see another car
                    else if (isCommand(result, yesCommandList)) {
                        Log.d(TAG, "customer wants to see another car");
                        try {
                            recognizer.removeGrammarConstraint(yesNoGrammar);
                            recognizer.addGrammarConstraint(categorySelectionGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        String categoryQuestion = buildCategoryString();
                        //MainActivity.getInstance().changeInfoText(categoryQuestion);
                        SpeakService.getInstance().speak(categoryQuestion);

                        dialogueStatus = DialogueStatus.CUSTOMER_INTERESTED;
                        return true;
                    }
                    return true;
                }

                // handle if customer wants to see call a salesman
                else if (dialogueStatus.equals(DialogueStatus.CALL_SALESMAN)) {

                    // handle if customer does not want to call a salesman
                    if (isCommand(result, noCommandList)) {
                        Log.d(TAG, "customer does not want to call a salesman");
                        try {
                            recognizer.addGrammarConstraint(additionalConsultationGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        String text = "Alright. Do you want to get additional consultation? We could offer you a phone call," +
                                " an appointment for a test drive or a sales offer.";
                        //MainActivity.getInstance().changeInfoText(text);
                        SpeakService.getInstance().speak(text);
                        dialogueStatus = DialogueStatus.ADDITIONAL_CONSULTATION;
                        return true;
                    }

                    // handle if customer wants to call a salesman
                    else if (isCommand(result, yesCommandList)) {
                        Log.d(TAG, "customer wants to call a salesman");
                        try {
                            recognizer.removeGrammarConstraint(yesNoGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        RecognitionService.getInstance().stopListening();
                        String text = "Alright, I will call a salesman. Please wait here, it will only take some minutes.";
                        //MainActivity.getInstance().changeInfoText(text);
                        SpeakService.getInstance().speak(text);
                        MainActivity.getInstance().sendMail();
                        MainActivity.getInstance().restart(false);
                        return false;
                    }

                    return true;
                }

                // handle if customer wants additional consultation like sales offer, phone call or test drive
                else if (dialogueStatus.equals(DialogueStatus.ADDITIONAL_CONSULTATION)) {

                    // handle if customer does not want additional consultation
                    if (isCommand(result, noCommandList)) {
                        Log.d(TAG, "customer does not want to additional consultation");
                        try {
                            recognizer.removeGrammarConstraint(yesNoGrammar);
                            recognizer.removeGrammarConstraint(additionalConsultationGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        RecognitionService.getInstance().stopListening();
                        String text = "Okay. Thank you! See you next time!";
                        //MainActivity.getInstance().changeInfoText(text);
                        SpeakService.getInstance().speak(text);
                        MainActivity.getInstance().restart(false);
                        return false;
                    }

                    // handle if customer wants additional consultation
                    else if (result.contains("offer") || result.contains("phone call") || result.contains("test drive")) {
                        Log.d(TAG, "customer wants additional consultation");
                        MainActivity.getInstance().customer = new Customer();
                        try {
                            recognizer.removeGrammarConstraint(additionalConsultationGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        if (result.contains("offer")) {
                            Log.d(TAG, "customer wants a sales offer");
                            MainActivity.getInstance().customer.setInterest("sales offer");
                        }
                        else if (result.contains("phone call")) {
                            Log.d(TAG, "customer wants a phone call");
                            MainActivity.getInstance().customer.setInterest("phone call");
                        }
                        else if (result.contains("test drive")) {
                            Log.d(TAG, "customer wants a test drive");
                            MainActivity.getInstance().customer.setInterest("test drive");
                        }
                        String text = "We need to collect some information from you to make an appointment or" +
                                " to send you an offer. Are you okay with that?";
                        //MainActivity.getInstance().changeInfoText(text);
                        SpeakService.getInstance().speak(text);
                        dialogueStatus = DialogueStatus.CONTACT_INFORMATION;
                        return true;
                    }
                    return true;
                }

                // handle contact information input
                else if (dialogueStatus.equals(DialogueStatus.CONTACT_INFORMATION)) {
                    boolean shouldRemove = false;

                    // handle if customer allows to collect customer data
                    if (isCommand(result, yesCommandList)) {
                        shouldRemove = true;
                        String text = "Please enter your contact information on the screen and confirm.";
                        //MainActivity.getInstance().changeInfoText(text);
                        SpeakService.getInstance().speak(text);
                        MainActivity.getInstance().switchScreen();
                    }

                    // handle if customer does not allow to collect customer data
                    else if (isCommand(result, noCommandList)) {
                        shouldRemove = true;
                        String text = "Okay. We are sorry that we cannot offer you additional consultation without your contact information. Thank you and see you next time!";
                        //MainActivity.getInstance().changeInfoText(text);
                        SpeakService.getInstance().speak(text);
                    }
                    if (shouldRemove) {
                        try {
                            Log.d(TAG, "remove grammar and stop listening");
                            recognizer.removeGrammarConstraint(yesNoGrammar);
                        } catch (VoiceException e) {
                            Log.e(TAG, "Exception: ", e);
                        }
                        //RecognitionService.getInstance().stopListening();
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
        Slot interest = new Slot( "interest ", false, Arrays.asList("Show me", "I would like to see", "Take me to", "Guide me to", "Can you show me",
                "I want to see"));
        Slot article = new Slot("article", false, Arrays.asList("the", "this", "that", "a", "an"));

        Slot category = new Slot("category name", false, Arrays.asList("hatchback", "coupe", "saloon", "cabriolet", "SUV", "MPV"));
        Slot modelName = new Slot("model name", true, Arrays.asList("car", "model", "A class", "B class", "C class", "CLA", "CLS", "S class",
                "E class", "G class", "GLA", "GLC","GLE","V class"));



        this.yesNoGrammar = new GrammarConstraint();
        this.yesNoGrammar.setName("yes no grammar");
        this.yesNoGrammar.addSlot(new Slot("yes", false, Arrays.asList("yes", "yes please", "yeah", "sure", "of course", "no", "no thanks", "nah", "nope")));

        this.categorySelectionGrammar = new GrammarConstraint();
        this.categorySelectionGrammar.setName("car selection grammar");
        this.categorySelectionGrammar.addSlot(interest);
        this.categorySelectionGrammar.addSlot(article);
        this.categorySelectionGrammar.addSlot(category);

        this.modelSelectionGrammar = new GrammarConstraint();
        this.modelSelectionGrammar.setName("model selection grammar");
        this.modelSelectionGrammar.addSlot(interest);
        this.modelSelectionGrammar.addSlot(article);
        this.modelSelectionGrammar.addSlot(modelName);

        this.questionInformationGrammar = new GrammarConstraint();
        this.questionInformationGrammar.setName("question information grammar");
        this.questionInformationGrammar.addSlot(new Slot("question", false, Arrays.asList("what is the", "tell me", "tell me the")));
        this.questionInformationGrammar.addSlot(new Slot("information type", false, Arrays.asList("general information about", "something about", "name", "color",
                "seat number", "power", "maximum speed", "transmission", "fuel type", "maximum fuel consumption", "price" )));
        this.questionInformationGrammar.addSlot(new Slot("car", true, Arrays.asList("of the car", "of this car", "this car", "the car")));

        this.additionalConsultationGrammar = new GrammarConstraint();
        this.additionalConsultationGrammar.setName("additional consultation grammar");
        this.additionalConsultationGrammar.addSlot(new Slot("wanting", false, Arrays.asList("I want to", "I would like to")));
        this.additionalConsultationGrammar.addSlot(new Slot("verb", false, Arrays.asList("receive", "do")));
        this.additionalConsultationGrammar.addSlot(new Slot("consultation", false, Arrays.asList("a sales offer", "a phone call", "a test drive")));
    }

    /**
     *  start the recognition
     */
    public void startListening() {
        Log.d(TAG, "start listening");
        this.dialogueStatus = DialogueStatus.START_DIALOGUE;
        try {
            this.recognizer.addGrammarConstraint(yesNoGrammar);
            this.recognizer.startRecognitionMode(this.recognitionListener);
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
     * prepare the category question to ask which category the customer is interested in
     * @return string
     */
    public String buildCategoryString() {
        StringBuilder builder = new StringBuilder();

        for (Category cat : MainActivity.getInstance().categories) {
            if (builder.length() == 0) {
                builder.append("Okay. What category are you interested in? ");
                builder.append(cat.getName());
            }
            else {
                builder.append(", " + cat.getName());
            }
        }
        builder.append(".");
        String string = builder.toString();
        return string;
    }

    /**
     * prepare the model question to ask which car model the customer is interested in
     * @return string
     */
    public String buildModelString() {
        StringBuilder builder = new StringBuilder();

        for (CarModel model : carModelOptions) {
            if (builder.length() == 0) {
                builder.append("Alright. Please tell me which model I should show you. Available models for this category are: ");
                builder.append(model.getName());
            }
            else {
                builder.append(", " + model.getName());
            }
        }
        builder.append(".");
        String string = builder.toString();
        return string;
    }

    /**
     * filter the map objects by the given category and add them to the optionsCar arraylist
     * @param cat
     * @return filtered list of cars which represent the options of the customer
     */
    private ArrayList<MapObject> filterMapObjectsByCategory(Category cat) {

        ArrayList<MapObject> carObjects = new ArrayList<>();
        for (MapObject obj : MainActivity.getInstance().cars) {
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

    /**
     *  end the recognition
     */
    public void stopListening() {
        Log.d(TAG, "stop listening");
        this.dialogueStatus = dialogueStatus.BEGINNING;
        this.carOptions = null;
        this.selectedMapObject = null;

        try {
            this.recognizer.stopRecognition();
        } catch (VoiceException e) {
            Log.e(TAG, "got VoiceException", e);
        }
    }

    /**
     *  disconnect the recognizer service
     */
    public void disconnect() {
        Log.d(TAG, "unbind recognizer service");
        this.recognizer.unbindService();
    }

    /**
     *  enum class which defines the dialogue status possibilities
     */
    private enum DialogueStatus {
        BEGINNING, START_DIALOGUE, CUSTOMER_INTERESTED, CATEGORY_SELECTED, MODEL_SELECTED, NEXT_CAR, MORE_INFORMATION, CALL_SALESMAN, ADDITIONAL_CONSULTATION, CONTACT_INFORMATION
    }
}

import com.sun.jna.Pointer;
import com.sun.jna.ptr.*;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;


/** Simple example of JNA interface mapping and usage. */
public class EmoStateLog 
{      
    public static void main(String[] args) 
    {
    	boolean COMPOSER = false;
    	boolean EPOC = true;
    	
    	//////////// MODE
    	boolean mode = COMPOSER;
    	
    	Pointer eEvent			= Edk.INSTANCE.EE_EmoEngineEventCreate();
    	Pointer eState			= Edk.INSTANCE.EE_EmoStateCreate();
    	IntByReference userID 	= null;
    	short composerPort		= (mode ? (short)3008 : (short)1726);
    	int option 				= (mode ? 2 : 1);
    	int state  				= 0;
    	Robot robot = null;
    	try{
    	robot = new Robot();
    	}
    	catch(Exception e){
    		System.out.println("Robot failure");
    	}
    	userID = new IntByReference(0);
    	
    	switch (option) {
		case 1:
		{
			if (Edk.INSTANCE.EE_EngineConnect("Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
				System.out.println("Emotiv Engine start up failed.");
				return;
			}
			break;
		}
		case 2:
		{
			System.out.println("Target IP of EmoComposer: [127.0.0.1] ");

			if (Edk.INSTANCE.EE_EngineRemoteConnect("127.0.0.1", composerPort, "Emotiv Systems-5") != EdkErrorCode.EDK_OK.ToInt()) {
				System.out.println("Cannot connect to EmoComposer on [127.0.0.1]");
				return;
			}
			System.out.println("Connected to EmoComposer on [127.0.0.1]");
			break;
		}
		default:
			System.out.println("Invalid option...");
			return;
    	}
    	
		while (true) 
		{
			state = Edk.INSTANCE.EE_EngineGetNextEvent(eEvent);

			// New event needs to be handled
			if (state == EdkErrorCode.EDK_OK.ToInt()) {

				int eventType = Edk.INSTANCE.EE_EmoEngineEventGetType(eEvent);
				Edk.INSTANCE.EE_EmoEngineEventGetUserId(eEvent, userID);

				// Log the EmoState if it has been updated
				if (eventType == Edk.EE_Event_t.EE_EmoStateUpdated.ToInt()) {

					Edk.INSTANCE.EE_EmoEngineEventGetEmoState(eEvent, eState);
					float timestamp = EmoState.INSTANCE.ES_GetTimeFromStart(eState);
					System.out.println(timestamp + " : New EmoState from user " + userID.getValue());
					
					System.out.print("WirelessSignalStatus: ");
					System.out.println(EmoState.INSTANCE.ES_GetWirelessSignalStatus(eState));
					
					int action = EmoState.INSTANCE.ES_CognitivGetCurrentAction(eState);
					double power = EmoState.INSTANCE.ES_CognitivGetCurrentActionPower(eState);
					if(power!=0)
					{
						if(action == EmoState.EE_CognitivAction_t.COG_LEFT.ToInt()){
							System.out.println("Left. Power: " + power);
							robot.keyPress(KeyEvent.VK_UP);
							robot.keyRelease(KeyEvent.VK_UP);
						}
						if(action == EmoState.EE_CognitivAction_t.COG_RIGHT.ToInt()){
							System.out.println("Right. Power: " + power);
							robot.keyPress(KeyEvent.VK_DOWN);
							robot.keyRelease(KeyEvent.VK_DOWN);
						}
					}
					if (EmoState.INSTANCE.ES_ExpressivIsBlink(eState) == 1)
						System.out.println("Blink");
					if (EmoState.INSTANCE.ES_ExpressivIsLeftWink(eState) == 1){
						System.out.println("LeftWink");
						robot.keyPress(KeyEvent.VK_LEFT);
						robot.keyRelease(KeyEvent.VK_LEFT);
					}
					if (EmoState.INSTANCE.ES_ExpressivIsRightWink(eState) == 1){
						System.out.println("RightWink");
						robot.keyPress(KeyEvent.VK_RIGHT);
						robot.keyRelease(KeyEvent.VK_RIGHT);
					}
					if (EmoState.INSTANCE.ES_ExpressivIsLookingLeft(eState) == 1)
						System.out.println("LookingLeft");
					if (EmoState.INSTANCE.ES_ExpressivIsLookingRight(eState) == 1)
						System.out.println("LookingRight");
					
					//System.out.print("ExcitementShortTerm: ");
					//System.out.println(EmoState.INSTANCE.ES_AffectivGetExcitementShortTermScore(eState));
					//System.out.print("ExcitementLongTerm: ");
					//System.out.println(EmoState.INSTANCE.ES_AffectivGetExcitementLongTermScore(eState));
					//System.out.print("EngagementBoredom: ");
					//System.out.println(EmoState.INSTANCE.ES_AffectivGetEngagementBoredomScore(eState));
					
					//System.out.print("CognitivGetCurrentAction: ");
					//System.out.println(EmoState.INSTANCE.ES_CognitivGetCurrentAction(eState));
					//System.out.print("CurrentActionPower: ");
					//System.out.println(EmoState.INSTANCE.ES_CognitivGetCurrentActionPower(eState));
				}
			}
			else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
				System.out.println("Internal error in Emotiv Engine!");
				break;
			}
		}
    	
    	Edk.INSTANCE.EE_EngineDisconnect();
    	System.out.println("Disconnected!");
    }
}


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Game Session, thread used to implement multiple game session
 * @author razanali
 */
public class GameSession extends Thread {

    //socket for players 
    Socket player1Socket;
    Socket player2Socket;
    //Scanner to read from players
    Scanner fromPlayer1;
    Scanner fromPlayer2;
    //PrintWriter to send data to players
    PrintWriter toPlayer1;
    PrintWriter toPlayer2;
    //varibles to save players points during the game 
    int player1point;
    int player2point;
    //number of turn of each player 
    int numOfTurnPlayer1;
    int numOfTurnPlayer2;
    //session number 
    int seesionNumber;
    //to indicate the current question number 
    int currentQuestionNumber;
    //game questions 
    String [] Questions = new String[10];
    //each question has 4 choices 
    String[][] choices = new String[10][4];
    //each choice can be true or false 
    //choiceStatus array save the status of choice [true/false]
    boolean[][] choiceStatus= new boolean[10][4];
    boolean exit ;//to end the game 
       
    
    //constructor the intilize parameters and intilize the quiz question 
    public GameSession(Socket player1, Socket player2, PrintWriter toPlayer1, PrintWriter toPlayer2, int seesionNumber) throws FileNotFoundException, IOException {
        
        this.player1Socket = player1;
        this.player2Socket = player2;
        fromPlayer1 =new Scanner (player1Socket.getInputStream());
        fromPlayer2 =new Scanner (player2Socket.getInputStream());
        this.fromPlayer1 = fromPlayer1;
        this.fromPlayer2 = fromPlayer2;
        this.toPlayer1 = toPlayer1;
        this.toPlayer2 = toPlayer2;
        this.seesionNumber=seesionNumber;
        intilizeQuestion();//call intilize question method 
        
    }

    
    /**
     * this method start the game 
     */
    @Override
    public void run() {
        
        //to indicate whoes trun now 
        boolean turn = true; //if trun = true player 1 trun, otherwise player2 turn
       
        while (!exit) {
            
            
            
            if (turn) { //player 1 turn 
                //call play method 
                boolean playerAnswer = play(fromPlayer1, toPlayer1, toPlayer2,1);
                toPlayer1.println(playerAnswer); //send to the player his answer 
                
                if(playerAnswer){ //if player 1 answered correctly 
                    player1point++;//increment points
                    //inform player 2 that player 1 answered correctly 
                    toPlayer2.println("Player 1 answered question No" + (currentQuestionNumber+1) + " Correctly!");
                }
                //if answer was wrong 
                else //inform player 2 that player 1 select wrong choice 
                    toPlayer2.println("Player 1 WRONG answer for question No." + (currentQuestionNumber+1) );
                //--
                turn = false; //change trun to false to let player 2 to play 
                numOfTurnPlayer1++;//increment number of turn of player 1
            }

            else {
                //call play method 
                boolean playerAnswer = play(fromPlayer2, toPlayer2, toPlayer1,2);
                toPlayer2.println(playerAnswer);//send to the player his answer 
                
                if(playerAnswer){//if player 1 answered correctly 
                    player2point++;//increment points
                    //inform player 2 that player 1 answered correctly 
                    toPlayer1.println("Player 2 answered question No" + (currentQuestionNumber+1)  + " Correctly!");
                }
                //if answer was wrong 
                else //inform player 2 that player 1 select wrong choice 
                    toPlayer1.println("Player 2 WRONG answer for question No." + (currentQuestionNumber+1) );
                //----
                turn = true; //change trun to true to let player 1 to play 
                numOfTurnPlayer2++;//increment number of turn of player 1
            }
            
            currentQuestionNumber++;//increment question No.
            
            if(checkResult())//check if there early end or not
                exit=true;
            
            if(currentQuestionNumber==10)//if all question was answerd 
                exit=true;//update exit[true] to  end the loop
           
        }
        gameResult();//find the result of the game session
        closeGameSession();//close the game
      
    }

    
    /**
     * this method play send the question to the player and wait for his answer 
     * 
     * 
     * @param player the player whose turn now
     * @param Toplayer to send data to the current player 
     * @param ToOpponent to send data to the opponent player 
     * @param playerNo to indicate which player play now player 1 or 2 
     * @return true if the player answered question correctly, otherwise return false 
     */
    private boolean play (Scanner player, PrintWriter Toplayer, PrintWriter ToOpponent, int playerNo){
        //print whose trun now at any session to the sever 
        Server.jTextArea1.append("Session "+seesionNumber+" number, player "+playerNo+" turn.\n");
         
        //notify the opponent that the other player trun now to wait 
        ToOpponent.println("Its now player "+playerNo+" turn!");
        //notify the player that his trun now 
        Toplayer.println("Your Turn!");
        //send a question to player 
        Toplayer.println(Questions[currentQuestionNumber]);
        //send all the 4 choices too 
        for(int i=0;i<4;i++)
            Toplayer.println(choices[currentQuestionNumber][i]);
      
        try{
            char playerChoice = player.nextLine().charAt(0);//read player answer 
            int choiceNumber = playerChoice - 65; //minus 65 from the choice to get  the index of the choice 
            return choiceStatus[currentQuestionNumber][choiceNumber]; //retrun the choice status 
        
        }catch(Exception ex){//if player quit the game and there is no response 
            exit=true;
            return false;   
        }
        
    }
    
    /**
     * this method to check if there is early result or not 
     * @return true to indicate there is an early end 
     */
    public boolean checkResult(){
        int difPoint= player1point-player2point; //find diffrent of point 
        if (numOfTurnPlayer2==3){
           if(difPoint==3 || difPoint==-3) //if the diffrent is 3 retrun true 
               return true;//
        }
        if (numOfTurnPlayer2==4){//if the number of truns is 4 
            //and the diffrent of points is 3 or 2 return true 
            if(difPoint==3 || difPoint==-3)
               return true;
           if(difPoint==2 || difPoint==-2)
               return true;
        }
        return false; //otherwise will return fasle [there is no early end]
    }
    /**
     * method to send result to player 
     */
    public void  gameResult(){
        //if player 1 has more points than 2 
        if(player1point>player2point){
            
            toPlayer1.println("YOU WON");
            toPlayer2.println("YOU LOST");
            //print result to the server 
            Server.jTextArea1.append("session number " + seesionNumber +" player 1 wons!\n");
        }
        //if player 2 has more points than 1
        else if (player2point>player1point ){
            toPlayer2.println("YOU WON");
            toPlayer1.println("YOU LOST");
            //print result to the server 
            Server.jTextArea1.append("session number " + seesionNumber +" player 2 wons!\n");
       
        }   
        //if they have the same number of points[DRAW]
        else if(player1point==player2point ){
            toPlayer1.println("DRAW");
            toPlayer2.println("DRAW"); 
            //print result to the server 
            Server.jTextArea1.append("session number " + seesionNumber +"  Result is DRAW!!\n");
        }
    
    
    
    }
    /**
     * this method read question from the input file and store it to the 
     * array of questions
     * @throws FileNotFoundException if file not found 
     */
    public void intilizeQuestion() throws FileNotFoundException{
        
            File inputFile = new File("CPCS371Quiz.txt");
            Scanner input = new Scanner(inputFile);//scanner to read input file 
            for (int i = 0; i < 10; i++) {
                Questions[i]= input.nextLine();//read question and store it to the array 
               
                for (int j = 0; j < 4; j++) {//read all the four choices 
                    choices[i][j]= input.nextLine();    
                }
                int number = input.nextLine().charAt(0) - 65;//find the index of the right answer
                choiceStatus[i][number] = true;//update its status to true 
                //the rest of the choices will remain as the default [false]
            }      
    }
    
    /**
     * method to close the game session
     */
    public void closeGameSession(){
        try {
            //player will recive close to close their program 
            toPlayer1.println("close!");
            toPlayer2.println("close!");
            //close socket of players
            player1Socket.close();
            player2Socket.close();
            
           
        } catch (IOException ex) {       
        }
    }
    

}
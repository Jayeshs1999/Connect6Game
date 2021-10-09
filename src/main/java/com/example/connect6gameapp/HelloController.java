package com.example.connect6gameapp;


import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HelloController implements Initializable {

    private static final int COLUMNS=10;
    private static final int ROWS=8;
    private static final int CIRCLE_DIAMETER=80;

    private static String PLAYER_ONE="Player One";
    private static String PLAYER_TWO="Player Two";
    @FXML
    private Button setNamesandColors;
    @FXML
    private TextField playerOneTextField;
    @FXML
    private TextField playerTwoTextField;

    @FXML
    private  ColorPicker playerOneColor=new ColorPicker();
    @FXML
    private  ColorPicker playerTwoColor=new ColorPicker();

    private static  String discColor1="#24303E";
    private static  String discColor2="#4CAA88";

    private boolean isPlayerOneTurn=true;

    private final Disc[][] insertedDiscsArray=new Disc[ROWS][COLUMNS];

    @FXML
    public GridPane rootGridPane;
    @FXML
    public Pane insertedDiscsPane;
    @FXML
    public Label playerNameLabel;

    private boolean isAllowedToInsert=true;

    public void createPlayground(){
        PLAYER_ONE="Player One";
        PLAYER_TWO="Player Two";
        Shape rectangleWithHoles=createGamesStructuralGrid();
        rootGridPane.add(rectangleWithHoles,0,1);
        ArrayList<Rectangle> rectangleList=createClickableColumn();
        for (Rectangle rectangle:rectangleList){
            rootGridPane.add(rectangle,0,1);
        }

        setNamesandColors.setOnAction(actionEvent -> {                                  //select Names and color choose by users
            PLAYER_ONE=playerOneTextField.getText();
            PLAYER_TWO=playerTwoTextField.getText();
            discColor1 ="#"+Integer.toHexString(playerOneColor.getValue().hashCode());   //get Player first color
            discColor2 ="#"+Integer.toHexString(playerTwoColor.getValue().hashCode());   //get player second color

            playerNameLabel.setText(PLAYER_ONE);

            if (playerOneTextField.getText().isEmpty() || playerTwoTextField.getText().isEmpty()
                    ||(playerOneTextField.getText()).equals(playerTwoTextField.getText())
                    ||playerOneColor.getValue() == playerTwoColor.getValue()) {
                Alert alert=new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Issue");
                alert.setHeaderText("Here you make some mistake :");
                alert.setContentText("1.Either you enter same names of both users or not enter any name," +
                        " so please enter different names.\n  Or" +
                        "\n2.You choose same color for both disc, so please choose different colors.");
                ButtonType okB=new ButtonType("Ok");
                alert.getButtonTypes().setAll(okB);
                alert.initStyle(StageStyle.UNDECORATED);
                Optional<ButtonType> btnT= alert.showAndWait();
                if (btnT.isPresent() && btnT.get()==okB){
                    resetGame();
                    createPlayground();
                    return;
                }
                alert.show();
            }
        });
    }

    private Shape createGamesStructuralGrid(){
        Shape rectangleWithHoles= new Rectangle((COLUMNS+1)*CIRCLE_DIAMETER,(ROWS+1)*CIRCLE_DIAMETER);

        for (int row=0;row<ROWS; row++){
            for (int col=0;col<COLUMNS;col++){
                Circle circle=new Circle();
                circle.setRadius(CIRCLE_DIAMETER/2);
                circle.setCenterX(CIRCLE_DIAMETER/2);
                circle.setCenterY(CIRCLE_DIAMETER/2);
                circle.setSmooth(true);

                circle.setTranslateX(col*(CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);
                circle.setTranslateY(row*(CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);
                rectangleWithHoles= Shape.subtract(rectangleWithHoles,circle);
            }
        }
        rectangleWithHoles.setFill(Color.WHITE);
        return rectangleWithHoles;
    }
    private ArrayList<Rectangle> createClickableColumn(){
        ArrayList<Rectangle> rectangleList=new ArrayList<Rectangle>();
        for (int col=0;col<COLUMNS;col++)
        {
            Rectangle rectangle=new Rectangle(CIRCLE_DIAMETER,(ROWS+1)*CIRCLE_DIAMETER);
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setTranslateX(col *(CIRCLE_DIAMETER+5)+(CIRCLE_DIAMETER/4));
            rectangle.setOnMouseEntered(mouseEvent -> rectangle.setFill(Color.valueOf("#eeeeee30")));
            rectangle.setOnMouseExited(mouseEvent -> rectangle.setFill(Color.TRANSPARENT));
            final int column=col;
            rectangle.setOnMouseClicked(mouseEvent -> {
                if (isAllowedToInsert){
                    isAllowedToInsert=false;
                    insertDisc(new Disc(isPlayerOneTurn),column);
                }
            });

            rectangleList.add(rectangle);
            //return rectangleList;
        }
        return rectangleList;
    }
    private void insertDisc(Disc disc ,int column){
        int row=ROWS-1;
        while(row>=0)
        {
            if (getDiscIsPresent(row,column)==null)
                break;
            row--;
        }
        if(row<0)
            return;

        insertedDiscsArray[row][column]=disc;//for stuctural change for developer
        insertedDiscsPane.getChildren().add(disc);
        disc.setTranslateX(column *(CIRCLE_DIAMETER+5)+(CIRCLE_DIAMETER/4));
        int currentRow=row;
        TranslateTransition translateTransition=new TranslateTransition(Duration.seconds(0.5),disc);
        translateTransition.setToY(row*(CIRCLE_DIAMETER+5)+CIRCLE_DIAMETER/4);
        translateTransition.setOnFinished(actionEvent -> {
            isAllowedToInsert=true;
            if(gameEnded(currentRow,column)){
                gameOver();
                return;
            }
            isPlayerOneTurn=!isPlayerOneTurn;
            playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE:PLAYER_TWO);
        });
        translateTransition.play();
    }
    private boolean gameEnded(int row,int column){
        //vertical point
        List<Point2D> verticalsPoint= IntStream.rangeClosed(row-5,row+5)
                .mapToObj(r->new Point2D(r,column))
                .collect(Collectors.toList());

        //horizontal point
        List<Point2D> horizontalPoints= IntStream.rangeClosed(column-5,column+5)
                .mapToObj(col->new Point2D(row,col))
                .collect(Collectors.toList());

        //Diagonals points
        Point2D startPoint1=new Point2D(row-5,column+5);
        List<Point2D> diagonalPoints=IntStream.rangeClosed(0,10)
                .mapToObj(i->startPoint1.add(i,-i))
                .collect(Collectors.toList());

        Point2D startPoint2=new Point2D(row-5,column-5);
        List<Point2D> diagonal2Points=IntStream.rangeClosed(0,10)
                .mapToObj(i->startPoint2.add(i,i))
                .collect(Collectors.toList());

        boolean isEnded=checkCombinations(verticalsPoint) || (checkCombinations(horizontalPoints))
                ||checkCombinations(diagonalPoints) ||checkCombinations(diagonal2Points);
        return isEnded;
    }

    private boolean checkCombinations(List<Point2D> points) {
        int chain=0;
        for (Point2D point:points){
            int rowIndexForArray=(int) point.getX();
            int columnIndexForArray=(int) point.getY();

            Disc disc=getDiscIsPresent(rowIndexForArray,columnIndexForArray) ;
            if(disc!=null && disc.isPlayerOnMove==isPlayerOneTurn) {
                chain++;
                if (chain == 6) {
                    return true;
                }
            }
            else{
                chain=0;
            }
        }
        return false;
    }
    private Disc getDiscIsPresent(int row,int column){
        if(row>=ROWS || row<0|| column>=COLUMNS || column<0)
            return null;
        return insertedDiscsArray[row][column];
    }

    private void gameOver(){
        String winnerPickColor=isPlayerOneTurn?discColor1:discColor2;    //Winner Color code
        String winner=isPlayerOneTurn ? PLAYER_ONE:PLAYER_TWO;
        // System.out.println("Winner is : "+winner);
        Alert alert=new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connect Four");
        alert.setHeaderText("The Winner is "+winner+"\t\t"+winnerPickColor);
        alert.setContentText("Want to Play again ?");
        ButtonType yesButton=new ButtonType("Yes");
        ButtonType exitButton=new ButtonType("No,Exit");
        alert.getButtonTypes().setAll(yesButton,exitButton);

        Platform.runLater(()->{
            Optional<ButtonType> btnClick= alert.showAndWait();
            if(btnClick.isPresent() && btnClick.get()==yesButton){
                resetGame();
            }
            else{
                Platform.exit();
                System.exit(0);
            }
        });
    }

    public void resetGame() {
        insertedDiscsPane.getChildren().clear();
        for (int row=0;row<insertedDiscsArray.length;row++){
            for (int col=0; col<insertedDiscsArray[row].length;col++){
                insertedDiscsArray[row][col]=null;
            }
        }
        playerOneColor.setValue(Color.valueOf("#24303E"));
        playerTwoColor.setValue(Color.valueOf("#4CAA88"));
        discColor1="#24303E";
        discColor2="#4CAA88";
        isPlayerOneTurn=true;
        playerNameLabel.setText("Player One");
        playerOneTextField.setText("");
        playerTwoTextField.setText("");
        //  playerNameLabel.setText(PLAYER_ONE);
        createPlayground();
    }
    private static class Disc extends Circle{
        private final boolean isPlayerOnMove;
        public Disc(boolean isPlayerOnMove){
//            if (playerOneColor.getValue()==null || playerTwoColor.getValue()==null) {
//                discColor1 = Integer.toHexString(playerOneColor.getValue().hashCode());
//                discColor2 = Integer.toHexString(playerTwoColor.getValue().hashCode());
//            }


            this.isPlayerOnMove=isPlayerOnMove;
            setRadius(CIRCLE_DIAMETER/2);
            setFill(isPlayerOnMove ? Color.valueOf(discColor1):Color.valueOf(discColor2));
            setCenterX(CIRCLE_DIAMETER/2);
            setCenterY(CIRCLE_DIAMETER/2);

        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
}
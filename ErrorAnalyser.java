package com.company;

import com.company.Token;

import java.security.PrivateKey;
import java.util.ArrayList;

public class ErrorAnalyser {

    private ArrayList<Token> tokensList;

    ErrorAnalyser(ArrayList<Token> tokens) {
        tokensList=tokens;
        searchErrors();
    }

    private void searchErrors() {
        ArrayList<Token> separators = new ArrayList<>();


        for (int i = 0; i < tokensList.size();i++) {
            if (tokensList.get(i).getItem().matches("\\(|\\)|\\{|}|\\[|]|;")) {
                separators.add(tokensList.get(i));
            }
        }
        int roundOpen = 0;
        int roundClose = 0;
        int squareOpen = 0;
        int squareClose = 0;
        int figureOpen = 0;
        int figureClose = 0;
        int noPoint = 0;
        for (Token item : separators) {

            if (item.getItem().matches("\\(")) {
                roundOpen+=1;
            } else if (item.getItem().matches("\\)")) {
                roundClose += 1;
            } else if (item.getItem().matches("\\{")) {
                figureOpen += 1;
            } else if (item.getItem().matches("}")) {
                figureClose += 1;
            } else if (item.getItem().matches("\\[")) {
                squareOpen += 1;
            } else if (item.getItem().matches("]")) {
                squareClose += 1;
            }
        }
//         == squareOpen == squareClose == figureOpen == figureClose == noPoint
        if (roundOpen == roundClose && squareOpen == squareClose && figureOpen == figureClose) {
            System.err.println("No errors");
        }


    }
}

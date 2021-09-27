package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private String tokens="abstract continue for new switch assert default if package " +
            "synchronized boolean do goto private this break double implements protected " +
            "throw byte else import public throws case enum instanceof return transient " +
            "catch extends int short try char final interface static void class finally " +
            "long strictfp volatile const float native super while";


    private final String separators="\\( \\) \\{ } \\[ ] ; , \\. \\.\\.\\. @ ::";
    //(=%1 )=%2 {=%3 }=%4 [=%5 ]=%6 .=%8 ...=%9
    private String operators="= > < ! ~ \\? : -> == >= <= != && \\|\\| \\+\\+ -- \\+ - \\* / & \\| \\^ % << >> >>> \\+= -= \\*= /= &= \\|= \\^= %= <<= >>= >>>=";
    //?=#1 ++=#2 +=#3 *=#4 ^=#5 +==#6 *==#7 ^==#8 ||=#9 |=#10 |==#11
    private final String inputStr;
    private final String[] defaultTokensList;
    private final String[] separatorsList;
    private final String[] operatorList;
    private final ArrayList<Token> allTokens = new ArrayList<>();
    private HashMap<String, String> literalsMap;


    private boolean isKeyword;
    private boolean isLiteral;
    private boolean isSeparator;
    private boolean isOperator;
    private int startCommonPoint = 0;
    private int startLinePoint = 0;
    private int endLinePoint = 0;
    private int columnPoint = 1;
    private final String[] lines;
    Parser(String codeString) {
        defaultTokensList = tokens.split(" ");
        separatorsList=separators.split(" ");
        operatorList=operators.split(" ");

        inputStr=codeString;
        lines =lineSeparator();
        for (int i =0; i < lines.length; i++) {
            isKeyword=false;
            isLiteral=false;
            isSeparator=false;
            isOperator=false;



            if (lines[i].equals("%4")) {
                endLinePoint+=1;
                startCommonPoint+=1;
                startLinePoint+=1;
            } else if (lines[i].equals("%6")) {
                endLinePoint+=2;
                startCommonPoint+=2;
                Token token = new Token("\\n","Enter",startLinePoint,endLinePoint,startCommonPoint,columnPoint);
                allTokens.add(token);
                startLinePoint=0;
                columnPoint+=2;
                endLinePoint=0;
            } else if (!lines[i].equals("")) {
                i+=checkIsKeyword(lines[i],i);
            }
        }

    }

    public String[] lineSeparator() {

        String newInput = inputStr.replace("\n"," %6 ");
        System.err.println(newInput.matches(".*\".*\".*"));

        boolean isBr=false;
        boolean isSmBr=false;
        boolean isComment = false;
        StringBuilder lit = new StringBuilder();
        literalsMap = new HashMap<>();
        StringBuilder str = null;
        int counter = 0;
        char previousSymb=' ';

        StringBuilder newInputStrHandMade = new StringBuilder();
        for (char c : inputStr.toCharArray()) {
            if (c=='"' && previousSymb!='\\' && !isComment) {
                if (!isBr) {
                    isBr = true;
                    str = new StringBuilder();
                    lit = new StringBuilder();
                }
                else {
                    lit.append(c);
                    str.append(lit);
                    literalsMap.put("literal"+counter,str.toString());
                    newInputStrHandMade.append("literal").append(counter);
                    counter++;
                    isBr=false;
                };
            } else if (c=='\'' && previousSymb!='\\' && !isComment) {
                if (!isSmBr) {
                    isSmBr = true;
                    str = new StringBuilder();
                    lit = new StringBuilder();
                }
                else {
                    lit.append(c);
                    str.append(lit);
                    literalsMap.put("literalSmall"+counter,str.toString());
                    newInputStrHandMade.append("literalSmall").append(counter);
                    counter++;
                    isSmBr=false;
                };
            } else if (c=='/' && previousSymb=='/' && !isComment) {
                isComment = true;
            }
            if (isBr || isSmBr) {
                lit.append(c);
            } else if (!isComment){
                if (c!='"' && c!='\'')
                    newInputStrHandMade.append(c);
            } else {
                if (c=='\n') {
                    isComment=false;
                }
            }
            previousSymb=c;
        }

        String inputStringWithoutLiteral = newInputStrHandMade.toString();


        String testStr=inputStringWithoutLiteral
                .replaceAll(" +"," ")
                .replace("("," ( ")
                .replace(")"," ) ")
                .replace("{"," { ")
                .replace("}"," } ")
                .replace("["," [ ")
                .replace("."," . ")
                .replace("..."," . ")
                .replace(";"," ; ")
                .replace("?"," ? ")
                .replace("++"," ++ ")
                .replaceAll("\\A=\\Z"," = ")
                .replaceAll("\\A\\+\\Z"," + ")
                .replaceAll("\\A\\*\\Z"," * ")
                .replaceAll("\\A\\^\\Z"," ^ ")
                .replace("+="," += ")
                .replace("/="," /= ")
                .replace("*="," *= ")
                .replace("^="," ^= ")
                .replaceAll("\\A\\|\\Z", " | ")
                .replace("||"," || ")
                .replace("|="," |= ")
                .replace(","," , ")
                .replace("\n"," %6 ");

        return testStr.split(" ");
    }

    public int checkIsKeyword(String line, int index) {
        for (String token : defaultTokensList) {
            Pattern keyword=Pattern.compile("\\A"+token+"\\Z");
            Matcher keywordMatcher = keyword.matcher(line);
            if (keywordMatcher.find()) {
                endLinePoint+=line.length();
                Token tokenItem = new Token(keywordMatcher.group(),token+"KeywordClass",startLinePoint,endLinePoint,startCommonPoint,columnPoint);
                allTokens.add(tokenItem);
                startCommonPoint+=line.length();
                startLinePoint+=line.length();
                isKeyword=true;
            }
        }
        if (!isKeyword) {
            index=checkIsLiteral(line, index);
        } else {
            index=0;
        }
        return index;
    }

    public int checkIsLiteral(String line, int i) {
        if (line.matches("\\d+")&&lines[i+1].matches("\\.|,")&&lines[i+2].matches("\\d{2,}")) {
            endLinePoint+=lines[i].length()+1+ lines[i+2].length();
            String literal = (lines[i] + lines[i+1] + lines[i+2]);
            Token token = new Token(literal,"FloatingPointLiteral",startLinePoint,endLinePoint,startCommonPoint,columnPoint);
            allTokens.add(token);
            startCommonPoint+=endLinePoint;
            startLinePoint=endLinePoint;
            i=2;
            isLiteral=true;
        } else if (line.matches("\\d+")) {
                if (line.matches("0x.*")) {
                    endLinePoint += line.length();
                    Token token = new Token(line,"DecimalHexLiteral", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                    allTokens.add(token);
                    startCommonPoint += endLinePoint;
                    startLinePoint = endLinePoint;
                    i = 0;
                    isLiteral = true;
                } else if (line.matches("0b.*")) {
                    endLinePoint += line.length();
                    Token token = new Token(line, "DecimalBinaryLiteral",startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                    allTokens.add(token);
                    startCommonPoint += endLinePoint;
                    startLinePoint = endLinePoint;
                    i = 0;
                    isLiteral = true;
                } else if (line.matches("0.*")) {
                    endLinePoint += line.length();
                    Token token = new Token(line,"DecimalOctalLiteral", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                    allTokens.add(token);
                    startCommonPoint += endLinePoint;
                    startLinePoint = endLinePoint;
                    i = 0;
                    isLiteral = true;
                } else {
                    endLinePoint += line.length();
                    Token token = new Token(line,"DecimalIntegerLiteral", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                    allTokens.add(token);
                    startCommonPoint += endLinePoint;
                    startLinePoint = endLinePoint;
                    i = 0;
                    isLiteral = true;
                }
        } else if (line.matches("literal\\d")) {
            StringBuilder literal= new StringBuilder();
            literal.append(literalsMap.get(line));
            endLinePoint += literal.length();
            Token token = new Token(literal.toString(),"StringLiteral", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
            allTokens.add(token);
            startCommonPoint += endLinePoint;
            startLinePoint = endLinePoint;
            i = 0;
            isLiteral = true;
        } else if (line.matches("literalSmall\\d")) {
            StringBuilder literal= new StringBuilder();
            literal.append(literalsMap.get(line));
            endLinePoint += literal.length();
            Token token = new Token(literal.toString(),"CharLiteral", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
            allTokens.add(token);
            startCommonPoint += endLinePoint;
            startLinePoint = endLinePoint;
            i = 0;
            isLiteral = true;
        }
        if (!isLiteral) {
            checkIsSeparator(line);
            return 0;
        }
        return i;
    }

    public void checkIsSeparator(String line) {
        for (String separator : separatorsList) {
            Pattern separators = Pattern.compile("\\A"+separator+"\\Z");
            Matcher separatorsMatcher = separators.matcher(line);
            if (separatorsMatcher.find()) {
                switch (separator) {
                    case "\\(": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("(", "LeftBracket", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "\\)": {
                        endLinePoint += 1;
                        Token tokenItem = new Token(")", "RightBracket", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "\\{": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("{", "leftFigBracket", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "\\[": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("[", "leftSquareBracket", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "\\.": {
                        endLinePoint += 1;
                        Token tokenItem = new Token(".", "Point", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "\\.\\.\\.": {
                        endLinePoint += 3;
                        Token tokenItem = new Token("...", "ThreePoint", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 3;
                        startLinePoint += 3;
                        break;
                    }
                    //                        "; , \\. \\.\\.\\. @ ::";
                    //(=%1 )=%2 {=%3 }=%4 [=%5 ]=%6 .=%8 ...=%9
                    case "}": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("}", "RightFigBracket", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 3;
                        startLinePoint += 3;
                        break;
                    } case "]": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("]", "RightSquareBracket", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 3;
                        startLinePoint += 3;
                        break;
                    }
                    case ";": {
                        endLinePoint += 1;
                        Token tokenItem = new Token(";", "PointNoPoint", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 3;
                        startLinePoint += 3;
                        break;
                    }case ",": {
                        endLinePoint += 1;
                        Token tokenItem = new Token(",", "NoPoint", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 3;
                        startLinePoint += 3;
                        break;
                    }
                    case "@": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("@", "OnDomain", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 3;
                        startLinePoint += 3;
                        break;
                    }
                    case "::": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("::", "TwoDoublePoints", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 3;
                        startLinePoint += 3;
                        break;
                    }
                }
                isSeparator=true;
            }
        }
        if (!isSeparator) {
            checkIsOperator(line);
        }
    }

    public void checkIsOperator(String line) {
        for (String operator : operatorList) {
            Pattern operators = Pattern.compile("\\A"+operator+"\\Z");
            Matcher operatorsMatcher = operators.matcher(line);
            if (operatorsMatcher.find()) {
                switch (operator) {
                    case "\\?": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("?", "QuestionMark", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "\\+\\+": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("++", "DoublePlus", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "\\+": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("+", "Plus", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "\\*": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("*", "Star", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "\\^": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("^", "Pow", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "\\+=": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("+=", "plusEqual", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "\\*=": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("*=", "StarEqual", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "\\^=": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("^=", "PowEqual", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "\\|\\|": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("||", "DoubleOr", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "\\|": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("|", "Or", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "\\|=": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("|=", "OrEqual", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "=": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("=", "equally", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case ">": {
                        endLinePoint += 1;
                        Token tokenItem = new Token(">", "More", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "<": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("<", "Less", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "!": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("!", "exclamationMark", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "~": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("~", "waveLine", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case ":": {
                        endLinePoint += 1;
                        Token tokenItem = new Token(":", "DoublePoints", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "->": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("->", "Arrow", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "==": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("==", "DoubleEqual", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case ">=": {
                        endLinePoint += 2;
                        Token tokenItem = new Token(">=", "MoreEqual", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "<=": {
                        endLinePoint += 2;
                        Token tokenItem = new Token(">=", "LessEqual", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "!=": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("!=", "exclamationEqual", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "&&": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("&&", "DoubleAnd", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "--": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("--", "DoubleMinus", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "-": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("-", "Minus", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "/": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("/", "Slash", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "&": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("&", "And", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "%": {
                        endLinePoint += 1;
                        Token tokenItem = new Token("%", "Precent", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 1;
                        startLinePoint += 1;
                        break;
                    }
                    case "<<": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("<<", "DoubleMore", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case ">>": {
                        endLinePoint += 2;
                        Token tokenItem = new Token(">>", "DoubleLess", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case ">>>": {
                        endLinePoint += 3;
                        Token tokenItem = new Token(">>>", "TripleLess", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 3;
                        startLinePoint += 3;
                        break;
                    }
                    case "-=": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("-=", "MinusEqual", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "/=": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("/=", "DivideEqual", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "&=": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("&=", "AndEqual", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "%=": {
                        endLinePoint += 2;
                        Token tokenItem = new Token("%=", "PrecentEqual", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 2;
                        startLinePoint += 2;
                        break;
                    }
                    case "<<=": {
                        endLinePoint += 3;
                        Token tokenItem = new Token("<<=", "DoubleLessEqual", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 3;
                        startLinePoint += 3;
                        break;
                    }
                    case ">>=": {
                        endLinePoint += 3;
                        Token tokenItem = new Token(">>=", "DoubleMoreEqual", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 3;
                        startLinePoint += 3;
                        break;
                    }
                    case ">>>=": {
                        endLinePoint += 4;
                        Token tokenItem = new Token(">>=", "TripleMoreEqual", startLinePoint, endLinePoint, startCommonPoint,columnPoint);
                        allTokens.add(tokenItem);
                        startCommonPoint += 4;
                        startLinePoint += 4;
                        break;
                    }
//                    "= > < ! ~ : -> == >= <= != && \\|\\| \\+\\+ -- \\+ - \\* / & \\| \\^ % << >> >>> \\+= -= \\*= /= &= \\|= \\^= %= <<= >>= >>>=";
                    //?=#1 ++=#2 +=#3 *=#4 ^=#5 +==#6 *==#7 ^==#8 ||=#9 |=#10 |==#11
                }
                isOperator=true;
            }
        }
        if (!isOperator) {
            endLinePoint+=line.length();
            Token tokenItem = new Token(line,"Identifier",startLinePoint,endLinePoint,startCommonPoint,columnPoint);
            allTokens.add(tokenItem);
            startCommonPoint+=line.length();
            startLinePoint+=line.length();
        }
    }

    public ArrayList<Token> getAllTokens() {
        return allTokens;
    }


}


package cn.czwfun.MyCCompiler.LexicalAnalysis;

import cn.czwfun.MyCCompiler.DFA.DFA;
import cn.czwfun.MyCCompiler.DFA.ICallBack;
import cn.czwfun.MyCCompiler.DFA.ITransRule;
import org.dom4j.DocumentHelper;

import java.io.*;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * Created by czwin on 2017/4/26.
 */
public class TokenScanner {

    private DFA dfa;
    private String now_type;
    private String now_s;
    private int line_num = 0; //行号
    private int cnt = 1; //token项计数

    private Element xmlroot;// = DocumentHelper.createElement("LexicalAnalysis");
    private Document xmldoc;// = DocumentHelper.createDocument(xmlroot);
    private Element xmltokens;// = xmlroot.addElement("tokens");


    Set<String> KeySet = new HashSet<String>() {{
        add("auto");
        add("break");
        add("case");
        add("char");
        add("const");
        add("continue");
        add("default");
        add("do");
        add("double");
        add("else");
        add("enum");
        add("extern");
        add("float");
        add("for");
        add("goto");
        add("if");
        add("int");
        add("long");
        add("register");
        add("return");
        add("short");
        add("signed");
        add("sizeof");
        add("static");
        add("struct");
        add("switch");
        add("typedef");
        add("union");
        add("unsigned");
        add("void");
        add("volatile");
        add("while");
        add("_Bool");
        add("_Complex");
        add("_Imaginary");
        add("inline");
        add("restrict");
        add("_Alignas");
        add("_Alignof");
        add(" _Atomic");
        add("_Generic");
        add("_Noreturn");
        add("_Static_assert");
        add("_Thread_local");
    }};
    HashMap<String,Element> hm = new HashMap<String,Element>();

    TokenScanner() {

        dfa = new DFA(170);
        ITransRule tr_id = (c) -> c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c < 'Z');
        ITransRule tr_dgt = (c) -> c >= '0' && c <= '9';
        ITransRule tr_i_d = (c) -> tr_id.isAbleToTrans(c) || tr_dgt.isAbleToTrans(c);
        ITransRule tr_not_i_d = (c) -> !tr_i_d.isAbleToTrans(c);
        ITransRule tr_not_dgt = (c) -> !tr_dgt.isAbleToTrans(c);
        ITransRule tr_nonzero = (c) -> c >= '1' && c <= '9';
        ITransRule tr_zero = (c) -> c =='0';
        ITransRule tr_oct = (c) -> c >= '0' && c <= '7';
        ITransRule tr_xX = (c) -> c == 'x' || c == 'X';
        ITransRule tr_unsigned_suf = (c) -> c == 'u' || c == 'U';
        ITransRule tr_long_suf = (c) -> c == 'l' || c == 'L';
        ITransRule tr_not_suf = (c) -> c != 'u' && c != 'U' && c != 'l' && c != 'L';
        ITransRule tr_notd_norsuf = (c) -> tr_not_dgt.isAbleToTrans(c) && c != 'u' && c != 'U' && c != 'l' && c != 'L';
        ITransRule tr_hex = (c) -> tr_dgt.isAbleToTrans(c) || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f';
        ITransRule tr_not_un = (c) -> !tr_unsigned_suf.isAbleToTrans(c);
        ITransRule tr_all = (c) -> true;
        ITransRule tr_not_long_suf = (c) -> c != 'l' && c != 'L';
        ITransRule tr_float_suf = c -> c == 'f' || c == 'l' || c == 'F' || c == 'L';
        ITransRule tr_eE = c -> c == 'e' || c == 'E';
        ITransRule tr_pP = c -> c == 'p' || c == 'P';

        //  integer-const 占用节点2-12
        ICallBack cb_int_const = (s) -> {
            now_type = "integer-const";
            now_s = s;
            UpdateXML();
        };
        dfa.addEdge(0,2,tr_nonzero);
        dfa.addEdge(2,2,tr_dgt);
        dfa.addEdge(2,120,c -> c == '.');
        dfa.addEdge(2,123,tr_eE);
        dfa.addEdge(2,cb_int_const,tr_notd_norsuf); //123
        dfa.addEdge(0,3,tr_zero);
        dfa.addEdge(3,4,tr_oct);
        dfa.addEdge(4,4,tr_oct);
        dfa.addEdge(4,121,tr_dgt);
        dfa.addEdge(4,123,tr_eE);

        dfa.addEdge(3,5,tr_xX);
        dfa.addEdge(5,6,tr_hex);
        dfa.addEdge(5,130,c -> c == '.');
        dfa.addEdge(6,130,c -> c == '.');
        dfa.addEdge(6,123,tr_pP);
        dfa.addEdge(6,6,tr_hex);
        dfa.addEdge(6,cb_int_const,tr_notd_norsuf); //0x7fff

        dfa.addEdge(2,7,tr_long_suf);
        dfa.addEdge(4,7,tr_long_suf);
        dfa.addEdge(6,7,tr_long_suf); //将三种情况连到后缀处理的部分

        dfa.addEdge(7,cb_int_const,tr_not_suf); //123l
        dfa.addEdge(7,10,tr_long_suf);
        dfa.addEdge(10,cb_int_const,tr_not_un); //123LL
        dfa.addEdge(7,12,tr_unsigned_suf);
        dfa.addEdge(10,12,tr_unsigned_suf);
        dfa.addEdge(12,cb_int_const); //123Lu 123llU

        dfa.addEdge(2,9,tr_unsigned_suf);
        dfa.addEdge(4,9,tr_unsigned_suf);
        dfa.addEdge(6,9,tr_unsigned_suf);

        dfa.addEdge(9,cb_int_const,tr_not_long_suf); //123u
        dfa.addEdge(9,8,tr_long_suf);
        dfa.addEdge(8,cb_int_const,tr_not_long_suf); //123uL
        dfa.addEdge(8,11,tr_long_suf);
        dfa.addEdge(11,cb_int_const); //123uLL
        dfa.addEdge(4,cb_int_const); //0777


        //operator 占用节点13-80 54个?
        ICallBack cb_operator = (s) -> {
            now_type = "operator";
            now_s = s;
            UpdateXML();
        };
        dfa.addEdge(0,13,c -> c=='[');
        dfa.addEdge(13,cb_operator);

        dfa.addEdge(0,14,c -> c==']');
        dfa.addEdge(14,cb_operator);

        dfa.addEdge(0,15,c -> c=='(');
        dfa.addEdge(15,cb_operator);

        dfa.addEdge(0,16,c -> c==')');
        dfa.addEdge(16,cb_operator);

        dfa.addEdge(0,17,c -> c=='.');
        dfa.addEdge(17,cb_operator,c -> c!='.'); // .
        dfa.addEdge(17,18,c -> c=='.');
        dfa.addEdge(18,19,c -> c=='.');
        dfa.addEdge(19,cb_operator); // ...

        dfa.addEdge(0,20, c -> c=='-');
        dfa.addEdge(20,21, c -> c=='>');
        dfa.addEdge(21,cb_operator); //->
        dfa.addEdge(20,22, c -> c=='-');
        dfa.addEdge(22,cb_operator); //--
        dfa.addEdge(20,23, c -> c == '=');
        dfa.addEdge(23,cb_operator); //-=
        dfa.addEdge(20,cb_operator); //-

        dfa.addEdge(0,24,c -> c=='+');
        dfa.addEdge(24,25,c -> c=='+');
        dfa.addEdge(25,cb_operator); //++
        dfa.addEdge(24,26,c -> c=='=');
        dfa.addEdge(26,cb_operator); //+=
        dfa.addEdge(24,cb_operator); //+

        dfa.addEdge(0,27,c -> c=='&');
        dfa.addEdge(27,28,c -> c=='&'); //&&
        dfa.addEdge(28,cb_operator);
        dfa.addEdge(27,29,c -> c=='='); //&=
        dfa.addEdge(27,cb_operator); //&

        dfa.addEdge(0,30,c -> c=='*');
        dfa.addEdge(30,31,c -> c=='=');
        dfa.addEdge(31,cb_operator); //*=
        dfa.addEdge(30,cb_operator); //*

        dfa.addEdge(0,32,c -> c == '~');
        dfa.addEdge(32,cb_operator); // ~

        dfa.addEdge(0,33,c -> c== '!');
        dfa.addEdge(33,34,c -> c== '=');
        dfa.addEdge(34,cb_operator); // !=
        dfa.addEdge(33,cb_operator); // !

        dfa.addEdge(0,35, c -> c == '/');
        dfa.addEdge(35,36, c -> c == '=');
        dfa.addEdge(36, cb_operator); // /= 22
        dfa.addEdge(35, cb_operator); // / 23

        dfa.addEdge(0,37,c -> c=='%');
        dfa.addEdge(37,38,c -> c=='=');
        dfa.addEdge(38,cb_operator); //%=
        dfa.addEdge(37,39,c -> c==':');
        dfa.addEdge(39,cb_operator); //%:
        dfa.addEdge(39,40,c -> c=='%');
        dfa.addEdge(40,41,c -> c==':');
        dfa.addEdge(41,cb_operator); //%:%:
        dfa.addEdge(37,67,c -> c == '>');
        dfa.addEdge(67, cb_operator); //%>
        dfa.addEdge(37,cb_operator); //%

        dfa.addEdge(0, 42, c -> c=='<');
        dfa.addEdge(42, 43, c -> c=='<');
        dfa.addEdge(43, 44, c -> c=='=');
        dfa.addEdge(44, cb_operator); // <<= 29
        dfa.addEdge(43, cb_operator); // <<
        dfa.addEdge(42, 45, c -> c==':');
        dfa.addEdge(45, cb_operator); // <:
        dfa.addEdge(42, 46, c -> c=='%');
        dfa.addEdge(46, cb_operator); // <%
        dfa.addEdge(42, 50, c -> c == '=');
        dfa.addEdge(50, cb_operator); // <=
        dfa.addEdge(42, cb_operator); // < 34

        dfa.addEdge(0, 47, c -> c == '>');
        dfa.addEdge(47, 48, c -> c == '>');
        dfa.addEdge(48, 49, c -> c == '=');
        dfa.addEdge(49, cb_operator); // >>=
        dfa.addEdge(48, cb_operator); // >>
        dfa.addEdge(47, 51, c -> c == '=');
        dfa.addEdge(51, cb_operator); // >=
        dfa.addEdge(47, cb_operator); // >

        dfa.addEdge(0, 52, c -> c == '=');
        dfa.addEdge(52, 53, c -> c == '=');
        dfa.addEdge(53, cb_operator); // ==
        dfa.addEdge(52, cb_operator); // =

        dfa.addEdge(0, 54, c -> c == '^');
        dfa.addEdge(54, 55, c -> c == '=');
        dfa.addEdge(55, cb_operator); // ^=
        dfa.addEdge(54, cb_operator); // ^

        dfa.addEdge(0, 55, c -> c == '|');
        dfa.addEdge(55,56, c -> c == '=');
        dfa.addEdge(56, cb_operator); //|=
        dfa.addEdge(55, 57, c -> c == '|');
        dfa.addEdge(57, cb_operator); // ||
        dfa.addEdge(55, cb_operator); // | 45

        dfa.addEdge(0, 58, c -> c == '?');
        dfa.addEdge(58, cb_operator); // ?

        dfa.addEdge(0, 59, c -> c == ':');
        dfa.addEdge(59, 60, c -> c == '>');
        dfa.addEdge(60,cb_operator); // :>
        dfa.addEdge(59, cb_operator); //>

        dfa.addEdge(0, 61, c -> c == ';');
        dfa.addEdge(61, cb_operator); // ;

        dfa.addEdge(0, 62, c -> c == ',');
        dfa.addEdge(62, cb_operator); // ,

        dfa.addEdge(0, 63, c -> c == '#');
        dfa.addEdge(63, 64, c -> c == '#');
        dfa.addEdge(64, cb_operator); // #
        dfa.addEdge(63, cb_operator); //##

        dfa.addEdge(0, 65, c -> c == '}');
        dfa.addEdge(65, cb_operator); // }
        dfa.addEdge(0, 66, c -> c == '{');
        dfa.addEdge(66, cb_operator); // { 54

        //string 占用节点编号 81-100
        ICallBack cb_string = (s) -> {
            now_type = "string-literal";
            now_s = s;
            UpdateXML();
        };
        ITransRule tr_simple_escape_seq = c -> c == '\'' || c == '\"' || c == '?' || c == '\\' || c == 'a' || c == 'b' || c =='f' || c == 'n' || c == 'r' || c == 't' || c == 'v';
        dfa.addEdge(0, 81, c -> c == '"');
        dfa.addEdge(0, 82, c -> c == 'u');
        dfa.addEdge(82,83, c -> c == '8');
        dfa.addEdge(82,81, c -> c == '"');
        dfa.addEdge(82,101,c -> c == '\''); // u'
        dfa.addEdge(82,1,tr_i_d);
        dfa.addEdge(83, 81, c -> c == '"');
        dfa.addEdge(83,1,tr_i_d);
        dfa.addEdge(0, 84, c -> c == 'U' || c == 'L');
        dfa.addEdge(84,81,c -> c == '"');
        dfa.addEdge(84, 101, c -> c == '\''); // L' U'
        dfa.addEdge(84,1,tr_i_d);
        dfa.addEdge(81,85,c -> c == '\\');
        dfa.addEdge(85,81,tr_simple_escape_seq);
        dfa.addEdge(81,94,c -> c == '"');
        dfa.addEdge(81,81,tr_all);
        dfa.addEdge(85,86,tr_oct);
        dfa.addEdge(86,87,tr_oct);
        dfa.addEdge(87,81,tr_oct); // \012
        dfa.addEdge(85,88,c -> c == 'x');
        dfa.addEdge(88,89,tr_hex);
        dfa.addEdge(89,81,tr_hex); //x1f
        dfa.addEdge(85,90,c -> c == 'u');
        dfa.addEdge(90,91,tr_hex);
        dfa.addEdge(91,92,tr_hex);
        dfa.addEdge(92,93,tr_hex);
        dfa.addEdge(93,81,tr_hex); //uffff
        dfa.addEdge(94,cb_string);

        //character-constant 占用节点编号101-113
        ICallBack cb_character = (s) -> {
            now_type = "character-constant";
            now_s = s;
            UpdateXML();
        };
        dfa.addEdge(0, 101, c -> c == '\'');
        dfa.addEdge(101,102,c -> c == '\\');
        dfa.addEdge(102,103,tr_simple_escape_seq);
        dfa.addEdge(103,104, c -> c == '\'');
        dfa.addEdge(104,cb_character);
        dfa.addEdge(101,105, c -> c != '\'');
        dfa.addEdge(105,104, c -> c == '\'');
        dfa.addEdge(102,106,tr_oct);
        dfa.addEdge(106,107,tr_oct);
        dfa.addEdge(107,105,tr_oct);
        dfa.addEdge(102,108,c -> c == 'x');
        dfa.addEdge(108,109,tr_hex);
        dfa.addEdge(109,105,tr_hex);
        dfa.addEdge(102,110,c -> c == 'u');
        dfa.addEdge(110,111,tr_hex);
        dfa.addEdge(111,112,tr_hex);
        dfa.addEdge(112,113,tr_hex);
        dfa.addEdge(113,105,tr_hex);


        //floating-constant 占用节点编号 120-140
        ICallBack cb_float = (s) -> {
            now_type = "floating-constant";
            now_s = s;
            UpdateXML();
        };
        dfa.addEdge(0,120,c -> c == '.');
        dfa.addEdge(121, 120, c -> c == '.');

        dfa.addEdge(121,121,tr_dgt);
        dfa.addEdge(121,122,tr_dgt);
        dfa.addEdge(122,122,tr_dgt);
        dfa.addEdge(122,123,tr_eE);
        dfa.addEdge(120,123,tr_eE);

        dfa.addEdge(121,123,tr_eE);
        dfa.addEdge(123,124,c -> c == '-' || c == '+');
        dfa.addEdge(124,125,tr_dgt);
        dfa.addEdge(123,125,tr_dgt);
        dfa.addEdge(125,125,tr_dgt);
        dfa.addEdge(120,126,tr_float_suf);
        dfa.addEdge(122,126,tr_float_suf);
        dfa.addEdge(125,126,tr_float_suf);
        dfa.addEdge(120,cb_float);
        dfa.addEdge(122,cb_float);
        dfa.addEdge(125,cb_float);
        dfa.addEdge(126,cb_float);

        dfa.addEdge(130,131,tr_hex);
        dfa.addEdge(131,131,tr_hex);
        dfa.addEdge(130,123,tr_pP);
        dfa.addEdge(131,123,tr_pP);



        // 标识符与关键字 0->1->ac
        dfa.addEdge(0,1,tr_id);
        dfa.addEdge(1,1,tr_i_d);
        dfa.addEdge(1,(s)->{
            now_type = KeySet.contains(s)?"keyword":"identifier";
            now_s = s;
            UpdateXML();
        },tr_not_i_d);
    }


    private void UpdateXML() {
        Element token = xmltokens.addElement("token");
        if(now_type == "identifier" && hm.containsKey(now_s) && hm.get(now_s).getName() != "") {
            Element ele = hm.get(now_s);
            List<Element> ls = ((Element)ele.elements().get(0)).elements();
            for(Element tmp: ls) {
                List<Element> tls = tmp.elements();
                token = xmltokens.addElement("token");
                for(Element tmp2: tls) {
                    if(tmp2.getName() == "number") {
                        token.addElement("number").addText(""+cnt++);
                    }
                    else if(tmp2.getName() == "line") {
                        token.addElement("line").addText(""+line_num);
                    }
                    else
                        token.addElement(tmp2.getName()).addText(tmp2.getText());
                }
            }
        }
        else {
            token.addElement("number").addText(""+cnt++);
            token.addElement("value").addText(now_s);
            token.addElement("type").addText(now_type);
            token.addElement("line").addText(""+line_num);
            token.addElement("valid").addText("true");
        }

    }


    public String run(String src, HashMap<String, Element> _hm) {

        hm = _hm;
        line_num = 0; //行号
        cnt = 1; //token项计数
        xmlroot = DocumentHelper.createElement("LexicalAnalysis");
        xmldoc = DocumentHelper.createDocument(xmlroot);
        xmltokens = xmlroot.addElement("tokens");

        xmlroot.addAttribute("src",src);
        String out_path = "";
        try {
            Scanner sc = new Scanner(new BufferedReader(new FileReader(src)));

            String text = "";
            while(sc.hasNextLine()) {
                line_num++;
                text = sc.nextLine() + " ";
                dfa.run(text, (s) -> {
                    System.out.println("    in line:" + line_num);
                    System.out.println("    processing in string s:" + s);
                });
            }

            now_s = "#";
            now_type = "#";
            line_num++;
            UpdateXML();

            OutputFormat of = new OutputFormat("    ",true);
            of.setEncoding("UTF-8");
            out_path = src.substring(0,src.lastIndexOf(".pp"))+".token.xml";
            XMLWriter xw = new XMLWriter(new FileOutputStream(out_path),of);
            xw.write(xmldoc);
            xw.close();
            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out_path;
    }

    public Element run(String content, boolean b) {

        line_num = 0; //行号
        cnt = 1; //token项计数
        xmlroot = DocumentHelper.createElement("LexicalAnalysis");
        xmldoc = DocumentHelper.createDocument(xmlroot);
        xmltokens = xmlroot.addElement("tokens");

        String out_path = "";
        try {
            dfa.run(content + " ", (s) -> {
                System.out.println("    in line:" + line_num);
                System.out.println("    processing in string s:" + s);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return xmlroot;
    }

    public static void main(String args[]) {
        TokenScanner ts = new TokenScanner();

    }
}

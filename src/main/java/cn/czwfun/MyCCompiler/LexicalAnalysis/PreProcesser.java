package cn.czwfun.MyCCompiler.LexicalAnalysis;

import cn.czwfun.MyCCompiler.DFA.DFA;
import cn.czwfun.MyCCompiler.DFA.ITransRule;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by czwin on 2017/4/26.
 */
public class PreProcesser {
    private String key = "";
    private int line_num;
    private HashMap<String,Element> hm;
    //# define identifier replacement-list new-line

    public HashMap<String,Element> getMap() {
        return hm;
    }

    public String run(String src) {
        String out_path = src.substring(0,src.lastIndexOf(".c"))+".pp";
        try {
            Scanner sc = new Scanner(new BufferedReader(new FileReader(src)));
            FileWriter fw = new FileWriter(out_path);
            String text = "";
            hm = new HashMap<String, Element>();
            line_num = 0;
            while(sc.hasNextLine()) {
                text = sc.nextLine();
                line_num++;
                //去掉开头无用空格或tab
                int pos = 0;
                try {
                    while(text.charAt(pos) == '\t' || text.charAt(pos) == ' ') pos++;
                    text = text.substring(pos);
                }
                catch (StringIndexOutOfBoundsException e) {
                    text = "";
                }
                int note = text.indexOf("//");
                if(note != -1) text = text.substring(0,note);

                if(text.indexOf("#define") == 0) {
                    ITransRule tr_id = (c) -> c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c < 'Z');
                    ITransRule tr_dgt = (c) -> c >= '0' && c <= '9';
                    ITransRule tr_i_d = (c) -> tr_id.isAbleToTrans(c) || tr_dgt.isAbleToTrans(c);
                    ITransRule tr_not_i_d = (c) -> !tr_i_d.isAbleToTrans(c);
                    String text2 = "";
                    try {
                        text2 = text.substring(7);
                        pos = 0;
                        while(text2.charAt(pos) == '\t' || text2.charAt(pos) == ' ') pos++;
                        text2 = text2.substring(pos);
                    }
                    catch (StringIndexOutOfBoundsException e) {
                        System.out.println("[err] [line:"+line_num+"] identifier requested.");
                        break;
                    }
                    int rlpos = text2.indexOf(' ');
                    if(rlpos == -1) rlpos = text2.length();
                    String text_rl = "";
                    try {
                        text_rl = text2.substring(rlpos);
                        pos = 0;
                        while(text_rl.charAt(pos) == '\t' || text_rl.charAt(pos) == ' ') pos++;
                        text_rl = text_rl.substring(pos);
                    }
                    catch (StringIndexOutOfBoundsException e) {
                        //System.out.println("[err] [line:"+line_num+"] replacement-list requested.");
                        text_rl = "";
                    }
                    DFA dfa = new DFA(10);
                    dfa.addEdge(0,1,tr_id);
                    dfa.addEdge(1,1,tr_i_d);
                    dfa.addEdge(1,(s)->{
                        key = s;
                    });
                    //System.out.println("id:"+text2.substring(0,rlpos));
                    dfa.run(text2.substring(0,rlpos) + " ", (s) -> {
                        System.out.println("    in line:" + line_num);
                        System.out.println("    processing in string s:" + s);
                    });
                    Element rl_ele = DocumentHelper.createElement("");
                    if(text_rl.length() > 0) {
                        pos = text_rl.length() - 1;
                        while (text_rl.charAt(pos) == ' ' || text_rl.charAt(pos) == '\t') pos--;
                        text_rl = text_rl.substring(0, pos + 1);// replacement-list
                        rl_ele = new TokenScanner().run(text_rl,true);
                    }
                    hm.put(key, rl_ele);
                    fw.write("\n");
                }
                else
                    fw.write(text + "\n"); //为了保证LA结果行号保证不变，即使空行也不做处理，若不使用空行使用行号映射复杂度较高
            }
            sc.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out_path;
    }
}

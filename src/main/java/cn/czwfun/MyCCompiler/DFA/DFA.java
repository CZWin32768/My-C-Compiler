package cn.czwfun.MyCCompiler.DFA;

import cn.czwfun.MyCCompiler.LexicalAnalysis.TokenScanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by czwin on 2017/4/26.
 */
public class DFA {
    private List<List<DFAEdge>> G; //存储状态转换图
    private int state_num = 0;


    public DFA(int _state_num) {
        state_num = _state_num;
        G = new ArrayList<List<DFAEdge>>(state_num);
        for(int i = 0; i < state_num; i++) {
            G.add(new ArrayList<DFAEdge>());
        }
    }

    public void addEdge(int fr, int to, ITransRule tr) {
        G.get(fr).add(new DFAEdge(to,tr));
    }
    public void addEdge(int fr, ICallBack icb, ITransRule tr) {
        G.get(fr).add(new DFAEdge(tr,icb));
    }
    public void addEdge(int fr, ICallBack icb) {G.get(fr).add(new DFAEdge(c -> true,icb));}

    public void run(String s, ICallBack cb_no_rule) {
        int stat = 0;
        String now = "";
        for(int i = 0; i < s.length(); i++) {
            boolean hasTrans = false;
            if(stat == 0 && s.charAt(i) == ' ') continue; // 处理多余空格问题 "int a" -> " a"
            for(DFAEdge df: G.get(stat)) {
                if(!df.isAbleToTrans(s.charAt(i))) continue;
                if(df.accepted()) {
                    df.runCallBack(now);
                    now = "";
                    stat = 0;
                    i--;
                }
                else {
                    now += s.charAt(i);
                    stat = df.getTarget();
                }
                hasTrans = true;
                break; // 找到对应的转移后跳出分表
            }
            if(!hasTrans) {
                System.out.println("[err] no rule to handle \""+s.charAt(i) + "\" now state: " + stat);
                cb_no_rule.run(now);
                break;
            }
        }
    }
    public static void main(String args[]) {
        DFA a = new DFA(10);
    }

}

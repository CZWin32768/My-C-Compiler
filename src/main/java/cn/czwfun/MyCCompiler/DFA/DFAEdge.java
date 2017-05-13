package cn.czwfun.MyCCompiler.DFA;

/**
 * Created by czwin on 2017/4/26.
 */
public class DFAEdge {
    private ITransRule trans_rule;
    private ICallBack icb;
    private int target;
    DFAEdge(int _target, ITransRule _trans_rule) {
        target = _target;
        trans_rule = _trans_rule;
    }
    DFAEdge(ITransRule _trans_rule, ICallBack _icb) { //若希望DFA的边指向处理程序，那么target置-1
        target = -1;
        trans_rule = _trans_rule;
        icb = _icb;
    }
    public boolean accepted() {
        return target == -1;
    }
    public boolean isAbleToTrans(char c) {
        return trans_rule.isAbleToTrans(c);
    }
    public int getTarget() {
        return target;
    }
    public void runCallBack(String s) {
        icb.run(s);
    }
}

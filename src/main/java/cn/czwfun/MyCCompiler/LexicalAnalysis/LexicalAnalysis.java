package cn.czwfun.MyCCompiler.LexicalAnalysis;

/**
 * Created by czwin on 2017/4/26.
 */
public class LexicalAnalysis {
    public static String LA(String src) {
        PreProcesser pp = new PreProcesser();
        String scan_src = pp.run(src);
        return new TokenScanner().run(scan_src, pp.getMap());
    }
}

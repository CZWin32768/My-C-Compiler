package cn.czwfun.MyCCompiler;

import cn.czwfun.MyCCompiler.LexicalAnalysis.LexicalAnalysis;
import cn.czwfun.MyCCompiler.LexicalAnalysis.TokenScanner;

/**
 * Created by czwin on 2017/4/26.
 */
public class MyCCompiler {
    public static void main(String args[]) {
        //LexicalAnalysis.LA("G:\\Homework\\compilertheory\\compilertheory\\exp2\\bit-minic-compiler\\input\\1\\test.c");
        if(args.length != 1) {
            System.out.println("usage: java -jar MyCCompiler [inputfile path]");
        }
        else
        {
            LexicalAnalysis.LA(args[0]);
            System.out.println("Lexical Analysis Finish.");
        }
    }
}

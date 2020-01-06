package org.warunek.Wordsplit;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * This was ported to java from https://github.com/keredson/wordninja from python
 *
 * @author Zach Warunek
 */
public class Wordsplit {
    ArrayList<String> words;
    HashMap wordcost;
    InputStream in;
    int maxword;
    public String split(String s){
        s = s.replaceAll("[^a-zA-Z0-9']+", "");
        String temp = s;
        s = s.toLowerCase();
        ArrayList<String> list = _split(s);
        int add = 0;
        for(int i = 0; i<list.size(); i++){
            try{
                list.set(i, temp.substring(add, list.get(i).length()+ add));
                add += list.get(i).length();
            }catch(IndexOutOfBoundsException e){
                list.set(i, temp.substring(add));
            }
        }
        StringBuilder sb = new StringBuilder();
        for(String str : list){
            str += " ";
            sb.append(str);
        }
        return sb.toString().trim();
    }
    //    # Build a cost dictionary, assuming Zipf's law and cost = -math.log(probability).
//    words = open("C:\\Users\\zacha\\Desktop\\wordlist.txt").read().split()
//    wordcost = dict((k, log((i+1)*log(len(words)))) for i,k in enumerate(words))
//    maxword = max(len(x) for x in words)
    public Wordsplit(File file) throws FileNotFoundException, IOException{
        in = new FileInputStream(file);
        wordcost = new HashMap();
        words = readFile();
        for(int i = 0; i<words.size(); i++){
            wordcost.put(words.get(i), Math.log((i+1) * Math.log(words.size())));
        }
        maxword = 0;
        words.stream().filter((str) -> (str.length() > maxword)).forEachOrdered((str) -> {
            maxword = str.length();
        });
    }
    //    def infer_spaces(s):
//        """Uses dynamic programming to infer the location of spaces in a string
//        without spaces."""
//        # Build the cost array.
//        cost = [0]
//        for i in range(1,len(s)+1):
//            c,k = best_match(i)
//            cost.append(c)
//
//        # Backtrack to recover the minimal-cost string.
//        out = []
//        i = len(s)
//        while i>0:
//            c,k = best_match(i)
//            assert c == cost[i]
//            out.append(s[i-k:i])
//            i -= k
//
//        return " ".join(reversed(out))
    public ArrayList<String> _split(String s){
        ArrayList<Double> cost = new ArrayList<>();
        ArrayList temp;
        double c;
        int k;


        cost.add(0.0);
        for(int i = 1; i<s.length() + 1; i++){
            temp = bestmatch(i, cost, s);
            c = (double)temp.get(0);
            cost.add(c);
        }


        ArrayList<String> out = new ArrayList<>();
        int i = s.length();
        while(i>0){
            temp = bestmatch(i, cost, s);
            k = (int)temp.get(1);
            boolean newToken = true;
//          # Apostrophe and digit handling (added by Genesys)
//          newToken = True
//          if not s[i-k:i] == "'": # ignore a lone apostrophe
//            if len(out) > 0:
//              # re-attach split 's and split digits
//              if out[-1] == "'s" or (s[i-1].isdigit() and out[-1][0].isdigit()): # digit followed by digit
//                out[-1] = s[i-k:i] + out[-1] # combine current token with previous token
//                newToken = False
//          # (End of Genesys addition)
            String substring = s.substring(i - k, i);
            if(!substring.equals("'"))
                if(out.size() > 0)
                    if(out.get(out.size()-1).equals("'s") || (Character.isDigit(s.charAt(i-1)) && Character.isDigit(out.get(out.size()-1).charAt(0)))){
                        out.set(out.size()-1, (String) substring + out.get(out.size()-1));
                        newToken = false;
                    }
            if(newToken)
                out.add(substring);
            i-=k;
        }
        Collections.reverse(out);
        return out;
    }
    //    # Find the best match for the i first characters, assuming cost has
//    # been built for the i-1 first characters.
//    # Returns a pair (match_cost, match_length).
//    def best_match(i):
//        candidates = enumerate(reversed(cost[max(0, i-maxword):i]))
//        for y, x in candidates:
//            print((x + wordcost.get(s[i-y-1:i], 9e999), y+1))
//        return min((c + wordcost.get(s[i-k-1:i], 9e999), k+1) for k,c in candidates)
    public ArrayList bestmatch(int i, ArrayList<Double> cost, String s){
        ArrayList<Double> temp = new ArrayList();
        if(0> i - maxword)
            for(Double ob : cost.subList(0, i))
                temp.add(ob);
        else
            for(Double ob : cost.subList(i - maxword, i))
                temp.add(ob);
        Collections.reverse(temp);
        double min = 9999999;
        int index = 1;
        for(int n = 0; n < temp.size(); n++){
            Double ob = temp.get(n);

            if(wordcost.get(s.substring(i-n-1, i)) != null)
                if((ob + (double)wordcost.get(s.substring(i-n-1, i))) < min){
                    min = (ob + (double)wordcost.get(s.substring(i-n-1, i)));
                    index = n + 1;
                }
        }
        return new ArrayList(Arrays.asList(min, index));
    }
    private ArrayList<String> readFile() throws IOException {
        in = new GZIPInputStream(in);
        Reader reader = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(reader);
        String line;
        ArrayList<String> words = new ArrayList();
        while((line = br.readLine()) != null)
            words.add(line);
        return words;
    }
}
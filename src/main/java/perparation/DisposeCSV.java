package perparation;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DisposeCSV {
    public static List<List<String>> removeCol(List<List<String>> list, int col){
        List<List<String>> result = new ArrayList<>();
        for(int k = 0; k < list.size(); k++){
            List<String> new_stringList = new ArrayList<>();
            List<String> stringList = list.get(k);
            for(int i = 0 ; i < stringList.size(); i++){
                if(i != col){
                    new_stringList.add(stringList.get(i));
                }
            }
            result.add(new_stringList);
        }
        return result;
    }
    public static List<List<String>> removeCol(List<List<String>> list, int col, int col1){
        List<List<String>> result = new ArrayList<>();
        for(int k = 0; k < list.size(); k++){
            List<String> new_stringList = new ArrayList<>();
            List<String> stringList = list.get(k);
            for(int i = 0 ; i < stringList.size(); i++){
                if(i != col && i != col1){
                    new_stringList.add(stringList.get(i));
                }
            }
            result.add(new_stringList);
        }
        return result;
    }
    public static List<List<String>> removeCol(List<List<String>> list, int col, int col1, int col2){
        List<List<String>> result = new ArrayList<>();
        for(int k = 0; k < list.size(); k++){
            List<String> new_stringList = new ArrayList<>();
            List<String> stringList = list.get(k);
            for(int i = 0 ; i < stringList.size(); i++){
                if(i != col && i != col1 && i != col2){
                    new_stringList.add(stringList.get(i));
                }
            }
            result.add(new_stringList);
        }
        return result;
    }
    public static List<List<String>> removeCol(List<List<String>> list, int col, int col1, int col2, int col3){
        List<List<String>> result = new ArrayList<>();
        for(int k = 0; k < list.size(); k++){
            List<String> new_stringList = new ArrayList<>();
            List<String> stringList = list.get(k);
            for(int i = 0 ; i < stringList.size(); i++){
                if(i != col && i != col1 && i != col2 && i !=col3){
                    new_stringList.add(stringList.get(i));
                }
            }
            result.add(new_stringList);
        }
        return result;
    }
    public static List<List<String>> removeCol(List<List<String>> list, int col, int col1, int col2, int col3,
                                               int col4, int col5, int col6, int col7){
        List<List<String>> result = new ArrayList<>();
        for(int k = 0; k < list.size(); k++){
            List<String> new_stringList = new ArrayList<>();
            List<String> stringList = list.get(k);
            for(int i = 0 ; i < stringList.size(); i++){
                if(i != col && i != col1 && i != col2 && i !=col3 &&
                        i !=col4 && i !=col5 && i !=col6 && i !=col7 ){
                    new_stringList.add(stringList.get(i));
                }
            }
            result.add(new_stringList);
        }
        return result;
    }


    public static List<List<String>> removeCol(List<List<String>> list, List<Integer> col){
        List<List<String>> result = new ArrayList<>();
        for(int k = 0; k < list.size(); k++){
            List<String> new_stringList = new ArrayList<>();
            List<String> stringList = list.get(k);
            for(int i = 0 ; i < stringList.size(); i++){
                if(!col.contains(i)){
                    new_stringList.add(stringList.get(i));
                }
            }
            result.add(new_stringList);
        }
        return result;
    }

    public static List<Integer> setCol(int begin, int end){
        List<Integer> col = new ArrayList<>();
        for(int i = begin; i <= end; i++){
            col.add(i);
        }
        return col;
    }

    public static List<List<String>> getSubList (List<List<String>>list,int begin, int row,boolean hasTitle){
        if(!hasTitle)
            return list.subList(begin, begin+row);
        else {
            List<List<String>> listList = list.subList(begin + 1, begin + row + 1);
            listList.add(0, list.get(0));
            return listList;
        }
    }

    public static List<List<String>> getSubListBySelect (List<List<String>> list,List<Integer> rows,boolean hasTitle){
        List<List<String>> result = new ArrayList<>();
        result.add(0, list.get(0));
        for(int i:rows) {
            result.add(list.get(i));
        }
        return result;
    }
    public static  List<List<String>> readCSV(String path) throws IOException {
        File csv = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(csv));
        String lineDta = "";
        List<List<String>> List = new ArrayList<>();

        while ((lineDta = br.readLine())!= null){
            List<String> stringListlist = Arrays.asList(lineDta.split(","));
            List.add(stringListlist);
        }
        return List;
    }

    public static void writeCSV(List<List<String>> result, String path) throws IOException {
        System.out.println("----------------------------------");
        System.out.println("写文件：");
        File csv2 = new File(path);//CSV文件
        BufferedWriter bw = new BufferedWriter(new FileWriter(csv2,true));
        for(int i = 0; i < result.size(); i++){
            String s0 = String.join(",",result.get(i));
            bw.write(s0);
            bw.newLine();
        }
        bw.close();
        System.out.println("写文件结束");
    }


    public static void main(String[] args) throws IOException {
        //读文件
        List<List<String>> lists = readCSV("Data/DB-100k-9.csv");

//        List<Integer> rows = Arrays.asList(892, 1268, 2923, 3829, 4864, 6834, 7249, 7514, 8547, 8893, 10747, 11106, 12568, 13496, 14185, 16343, 16528, 17343, 17563, 17850, 17989, 18337, 18637, 20822, 23808, 24846, 25255, 25836, 25926, 28607, 28861, 29607, 29915, 31959, 36805, 37920, 39865, 40354, 41093, 41218, 42512, 44538, 45148, 48021, 48852, 51386, 51867, 51974, 53721, 55593, 60803, 60886, 61508, 63134, 67232, 73335, 74536, 74550, 75079, 75315, 76084, 76725, 77811, 82030, 84367, 84658, 85470, 86132, 89182, 92959, 90483, 94555, 95773, 96519, 96966, 97225, 97629);
//        List<List<String>> result = getSubListBySelect(lists,rows,true);
//        System.out.println(result.size());
        //只保留93w条数据
       List<List<String>> result = getSubList(lists,0,49922, true);

        //删除列
//              List<Integer> col = Arrays.asList(1,2,4,5,7,8,9,10,11,12,13,14,15,17,18,20,21,22,23,24,25,26,27,28,29);
//        List<Integer> col = setCol(13,16);
//        result = removeCol(result,col);
//        List<List<String>> result = removeCol(lists,col);
//        int len = result.get(0).size();
//        List<String> new_title = new ArrayList<>();
//        for(int i = 0; i < len; i++){
//            new_title.add(String.valueOf(i));
//        }
//        result.remove(0);
//        result.add(0, new_title);

        //写文件
        writeCSV(result, "Data/DB-50k-9.csv");
    }




}

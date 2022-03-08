package util;


public class Timer {
    private long begin;
    public void reset(){
        begin=System.currentTimeMillis();
    }
    public Timer(){
        reset();
    }
    public void outTime(){
        System.out.println(System.currentTimeMillis()-begin);
    }
    public void outTimeAndReset(){
        outTime();
        reset();
    }
    public long getTimeUsed(){
        return System.currentTimeMillis()-begin;
    }
    public long getTimeUsedAndReset(){
        long result= getTimeUsed();
        reset();
        return result;
    }
}

package cn.boxfish.log2.analysis;

import com.google.common.base.Strings;

/**
 * Created by lvtu on 2016/11/11.
 */
public class MethodNode {
    private String pkg;
    private String rtn;
    private long cost;
    private String logStartTime;
    private String logEndTime;

    MethodNode(String pkg){
        this.pkg = pkg;
    }

    MethodNode(String pkg,String logStartTime){
        this.pkg = pkg;
        this.logStartTime = logStartTime;
    }

    public void labelEnd(String rtn){
        this.rtn = rtn;
    }

    public boolean isEnd(){
        return !Strings.isNullOrEmpty(rtn);
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getRtn() {
        return rtn;
    }

    public void setRtn(String rtn) {
        this.rtn = rtn;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public String getLogStartTime() {
        return logStartTime;
    }

    public void setLogStartTime(String logStartTime) {
        this.logStartTime = logStartTime;
    }

    public String getLogEndTime() {
        return logEndTime;
    }

    public void setLogEndTime(String logEndTime) {
        this.logEndTime = logEndTime;
    }

    @Override
    public String toString() {
        return "MethodNode{" +
                "pkg='" + pkg + '\'' +
                ", rtn='" + rtn + '\'' +
                ", cost=" + cost +
                ", logStartTime='" + logStartTime + '\'' +
                ", logEndTime='" + logEndTime + '\'' +
                '}';
    }
}

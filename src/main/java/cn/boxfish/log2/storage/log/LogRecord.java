package cn.boxfish.log2.storage.log;

/**
 * Created by lvtu on 2016/11/2.
 */
public class LogRecord {
    private String id;
    private String time;
    private String thread;
    private String level;
    private String loggerName;
    private String content;
    private String line;

    public static LogRecord of(String time,
                               String thread,
                               String level,
                               String loggerName,
                               String content,
                               String line){
        LogRecord l = new LogRecord();
        l.time = time;
        l.thread = thread;
        l.level = level;
        l.loggerName = loggerName;
        l.content = content;
        l.line = line;
        return l;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getThread() {
        return thread;
    }

    public void setThread(String thread) {
        this.thread = thread;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return "LogAnalysis{" +
                "id=" + id +
                ", time='" + time + '\'' +
                ", thread='" + thread + '\'' +
                ", level='" + level + '\'' +
                ", loggerName='" + loggerName + '\'' +
                ", content='" + content + '\'' +
                ", line='" + line + '\'' +
                '}';
    }
}

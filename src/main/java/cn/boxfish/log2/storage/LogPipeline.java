package cn.boxfish.log2.storage;

import cn.boxfish.log2.analysis.Analysis;
import cn.boxfish.log2.storage.log.LogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lvtu on 2016/11/15.
 */
@Component
public class LogPipeline {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoOperations mongoOperations;

    private final static Logger logger = LoggerFactory.getLogger(LogPipeline.class);

    private final static Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) (\\[(\\w|-)+\\]) (INFO|ERROR|WARN|DEBUG|TRACE)  ((\\w|\\.|\\(|\\))+) (.+)");

    private final static Pattern tIdPattern = Pattern.compile("t_id=(\\d+)");
    public static String get_tId(String line){
        Matcher matcher = tIdPattern.matcher(line);
        if(matcher.find()){
            return matcher.group(1);
        }
        return "";
    }

    private ArrayBlockingQueue<LogRecord> buffer = new ArrayBlockingQueue<LogRecord>(1000);


    public void transform(InputStream in) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(in));

            String time = null;
            String thread = null;
            String level = null;
            String loggerName = null;
            String content = null;
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    time = matcher.group(1);
                    thread = matcher.group(2);
                    level = matcher.group(4);
                    loggerName = matcher.group(5);
                    content = matcher.group(7);
                    LogRecord logRecord = LogRecord.of(time,
                            thread, level, loggerName, content, get_tId(line),line);
                    logger.info("time:{},thread:{},level:{},loggerName:{},content:{}", time, thread, level, loggerName, content);
                    fillBuffer(logRecord);
                }
            }

            flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try{
                    bufferedReader.close();
                }catch (Exception e){
                    //ignore ex
                }
            }
        }
    }

    private void fillBuffer(LogRecord analysisDomain) {
        if (!buffer.offer(analysisDomain)) {
            sendBuffer();
        }
    }

    private void sendBuffer() {
        if (!buffer.isEmpty()) {
            mongoOperations.insert(buffer, LogRecord.class);
            buffer.clear();
        }
    }

    private void flush() {
        sendBuffer();
    }
}

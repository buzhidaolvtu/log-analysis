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


    private ArrayBlockingQueue<LogRecord> buffer = new ArrayBlockingQueue<LogRecord>(5);


    public void transform(String line) {
        try {
            URL resource = Analysis.class.getClassLoader().getResource("log2.log");
            Path path = Paths.get(resource.toURI());
            FileReader fileReader = new FileReader(path.toFile());
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String time = null;
            String thread = null;
            String level = null;
            String loggerName = null;
            String content = null;

            while ((line = bufferedReader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    time = matcher.group(1);
                    thread = matcher.group(2);
                    level = matcher.group(4);
                    loggerName = matcher.group(5);
                    content = matcher.group(7);
                    LogRecord logRecord = LogRecord.of(time,
                            thread, level, loggerName, content, line);
                    logger.info("time:{},thread:{},level:{},loggerName:{},content:{}", time, thread, level, loggerName, content);
                    fillBuffer(logRecord);
                }
            }

            flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillBuffer(LogRecord analysisDomain) {
        if (!buffer.offer(analysisDomain)) {
            sendBuffer();
        }
    }

    private void sendBuffer() {
        if (!buffer.isEmpty()) {
            mongoOperations.insert(buffer,LogRecord.class);
            buffer.clear();
        }
    }

    private void flush() {
        sendBuffer();
    }
}

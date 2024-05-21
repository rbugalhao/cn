package children;


import com.google.cloud.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Image {

    public String bucket;
    public String blob;
    public Date date;
    public List<String> labels;
    public List<String> translatedLabels;

    public Image() {}
}

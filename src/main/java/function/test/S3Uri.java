package function.test;

/**
 *
 * @author nick
 */
public class S3Uri {

    String e_tag;
    String size;
    String bucket;
    String key;

    public S3Uri(String e_tag, String size, String bucket, String key) {
        this.e_tag = e_tag;
        this.size = size;
        this.bucket = bucket;
        this.key = key;
    }
}

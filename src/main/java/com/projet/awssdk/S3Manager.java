package com.projet.awssdk;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;

import java.util.ArrayList;
import java.util.List;

public class S3Manager {
    private final AmazonS3 s3;

    public S3Manager(AmazonS3 s3) {
        this.s3 = s3;
    }

    public Iterable<String> getBucketsNames() {
        List<Bucket> buckets = s3.listBuckets();
        List<String> bucketsNames = new ArrayList<String>(buckets.size());

        for (Bucket bucket : buckets) {
            bucketsNames.add(bucket.getName());
        }

        return bucketsNames;
    }
}

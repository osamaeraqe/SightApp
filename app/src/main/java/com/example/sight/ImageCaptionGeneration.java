package com.example.sight;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.sight.ml.CnnModel;
import com.example.sight.ml.Model;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class ImageCaptionGeneration {

    private static String getJsonFromAssets(Context context, String fileName) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return jsonString;
    }

    private static ArrayList<Integer> tokenize(String message, HashMap<String, Integer> data) {
        String[] parts = message.split(" ");
        ArrayList<Integer> tokenizedMessage = new ArrayList<Integer>();
        for (String part : parts) {
            Integer index = 0;
            if (!data.containsKey(part)) {
                index = 0;
            } else {
                index = data.get(part);
            }
            tokenizedMessage.add(index);
        }
        return tokenizedMessage;
    }

    private static ArrayList<Integer> padSequence(ArrayList<Integer> sequence) {
        int maxlen = 32;
        if (sequence.size() > maxlen) {
            return (ArrayList<Integer>) sequence.subList(0, maxlen);
        } else if (sequence.size() < maxlen) {
            ArrayList<Integer> array = new ArrayList<Integer>();
            int temp = sequence.size();
            for (int i = 0; i < maxlen - temp; ++i) {
                array.add(0);
            }
            for (int i = 0; i < temp; ++i) {
                array.add(sequence.get(i));
            }
            return array;
        } else {
            return sequence;
        }
    }

    private static ByteBuffer convertImgToByteBuffer(Bitmap img) {
        //int width = img.getWidth();
        //int height = img.getHeight();
        //int size = Math.min(height, width);
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        //     .add(new ResizeWithCropOrPadOp(299, 299))
                        .add(new ResizeOp(299, 299, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                        .add(new NormalizeOp(127.5f, 127.5f))
                        .build();
        TensorImage tImage = new TensorImage(DataType.FLOAT32);
        tImage.load(img);

        tImage = imageProcessor.process(tImage);
        return tImage.getBuffer();
    }

    private static ByteBuffer convertImgToByteBuffer(Bitmap img, int size) {
        ByteBuffer byteBuffer1 = ByteBuffer.allocateDirect(4 * img.getWidth() * img.getHeight() * 3);
        byteBuffer1.order(ByteOrder.nativeOrder());
        int[] intValues = new int[img.getWidth() * img.getHeight()];
        img.getPixels(intValues, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
        int pixel = 0;
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                int val = intValues[pixel++];
                byteBuffer1.putFloat((float) ((((val & 0xff0000) >> 16) - 127.5) / 127.5));
                byteBuffer1.putFloat((float) ((((val & 0xff00) >> 8) - 127.5) / 127.5));
                byteBuffer1.putFloat((float) (((val & 0xff) - 127.5) / 127.5));
            }
        }
        return byteBuffer1;
    }


    private static String word_for_id(int pos, HashMap<String, Integer> data) {
        for (String key : data.keySet()) {
            // search  for value
            Integer value = data.get(key);
            if (value != null && value == pos) {
                return key;
            }
        }
        return "";
    }

//    public static Bitmap resizeImage(Bitmap realImage, float maxImageSize,
//                                     boolean filter) {
//        float ratio = Math.min(
//                (float) maxImageSize / realImage.getWidth(),
//                (float) maxImageSize / realImage.getHeight());
//        int width = Math.round((float) ratio * realImage.getWidth());
//        int height = Math.round((float) ratio * realImage.getHeight());
//
//        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
//                height, filter);
//        return newBitmap;
//    }

    private static ByteBuffer extractImgFeature(Bitmap img, Context context) throws IOException {
        CnnModel cnn_model = CnnModel.newInstance(context);
        TensorBuffer cnnInputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 299, 299, 3}, DataType.FLOAT32);
        ByteBuffer byteBuffer1 = convertImgToByteBuffer(img);
        cnnInputFeature0.loadBuffer(byteBuffer1);
        CnnModel.Outputs cnn_output = cnn_model.process(cnnInputFeature0);
        TensorBuffer output_image_features = cnn_output.getOutputFeature0AsTensorBuffer();
        cnn_model.close();
        return output_image_features.getBuffer();
    }

    public static String generateCaption(Bitmap img, Context context) throws IOException, JSONException {

        // Init The Models and TensorBuffers

        Model model = Model.newInstance(context);

        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32}, DataType.FLOAT32);
        TensorBuffer inputFeature1 = TensorBuffer.createFixedSize(new int[]{1, 2048}, DataType.FLOAT32);

        //ByteBuffer Holding the Image Features
        ByteBuffer imgFeatures = extractImgFeature(img, context);

        inputFeature1.loadBuffer(imgFeatures);

        //Opening and preparing the Tokenizer
        JSONObject jsonObject = new JSONObject(Objects.requireNonNull(getJsonFromAssets(context, "tokenizer.json")));
        HashMap<String, Integer> data = new HashMap<>();
        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            data.put(key, (Integer) jsonObject.get(key));
        }

        //Predicting the Caption
        StringBuilder message_text = new StringBuilder("start");
        for (int y = 0; y < 32; ++y) {
            ArrayList<Integer> tokenizedMessage = tokenize(message_text.toString().toLowerCase().trim(), data);
            ArrayList<Integer> paddedMessage = padSequence(tokenizedMessage);
            ByteBuffer byteBuffer0 = ByteBuffer.allocate(paddedMessage.size() * 4);
            byteBuffer0.order(ByteOrder.nativeOrder());
            for (Integer f : paddedMessage) {
                byteBuffer0.putFloat((float) (f));
            }
            inputFeature0.loadBuffer(byteBuffer0);
            Model.Outputs model_output = model.process(inputFeature0, inputFeature1);
            TensorBuffer prediction = model_output.getOutputFeature0AsTensorBuffer();
            float[] new_prediction = prediction.getFloatArray();
            float max = new_prediction[0];
            int pos_max = 0;
            for (int i = 1; i < new_prediction.length; i++) {
                if (new_prediction[i] > max) {
                    max = new_prediction[i];
                    pos_max = i;
                }
            }
            String word = word_for_id(pos_max, data);
            if (word.equals("")) break;
            message_text.append(" ").append(word);
            if (word.equals("end"))
                break;
        }
        model.close();
        StringBuilder final_text = new StringBuilder();
        String[] final_ans = message_text.toString().split(" ");
        for (int i = 1; i < final_ans.length - 1; ++i) {
            final_text.append(final_ans[i]);
            if (i < final_ans.length - 2)
                final_text.append(" ");
        }

        Log.d("Caption", message_text.toString());
        return final_text.toString();
    }

}

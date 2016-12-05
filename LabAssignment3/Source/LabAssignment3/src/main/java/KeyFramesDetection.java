import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.video.Video;
import org.openimaj.video.xuggle.XuggleVideo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by santu_pc on 10/15/2016.
 */
public class KeyFramesDetection {
    static Video<MBFImage> VideoFile;
    static List<MBFImage> FramesList = new ArrayList<MBFImage>();
    static List<Double> mainPoints = new ArrayList<Double>();
    public static void main(String args[]) throws IOException {
        String VideoFilePath = "data/BackStroke.mkv";
        Frames(VideoFilePath);
        KeyFrames();
        ObjectDetection objectDetection = new ObjectDetection();
    }
    public static void Frames(String VideoPath) throws IOException {
        int j = 1;
        VideoFile = new XuggleVideo(new File(VideoPath));
        for (MBFImage mbfImage : VideoFile){
            BufferedImage bufferedImage = ImageUtilities.createBufferedImageForDisplay(mbfImage);
            String OutputFrameName = "Output/frames/" + j + ".jpg";
            File OutputFrame = new File(OutputFrameName);
            try {
                ImageIO.write(bufferedImage, "jpg", OutputFrame);
            }
            catch (IOException e){
                e.printStackTrace();
            }
            MBFImage CloneImage = mbfImage.clone();
            FramesList.add(CloneImage);
            j++;
        }
    }
    public static void KeyFrames(){
        for (int i = 0; i < FramesList.size() - 1; i++) {
            MBFImage SourceImage = FramesList.get(i);
            MBFImage NextImage = FramesList.get(i + 1);
            DoGSIFTEngine doGSIFTEngine = new DoGSIFTEngine();
            LocalFeatureList<Keypoint> SourceEstimator = doGSIFTEngine.findFeatures(SourceImage.flatten());
            LocalFeatureList<Keypoint> NextEstimator = doGSIFTEngine.findFeatures(NextImage.flatten());
            RobustAffineTransformEstimator modelFitter = new RobustAffineTransformEstimator(0.5, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5));
            LocalFeatureMatcher<Keypoint> localFeatureMatcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
                    new FastBasicKeypointMatcher<Keypoint>(8), modelFitter);
            localFeatureMatcher.setModelFeatures(SourceEstimator);
            localFeatureMatcher.findMatches(NextEstimator);
            double size = localFeatureMatcher.getMatches().size();
            mainPoints.add(size);
        }
        Double maxPoint = Collections.max(mainPoints);
        for (int i = 0; i < mainPoints.size(); i++){
            if (((mainPoints.get(i)/maxPoint) < 0.01) || i == 0){
                Double keyMainPoint = mainPoints.get(i)/maxPoint;
                BufferedImage bufferedFrame = ImageUtilities.createBufferedImageForDisplay(FramesList.get(i+1));
                String name = "Output/KeyFrames/" + i + "_" + keyMainPoint.toString() + ".jpg";
                File outputFile = new File(name);
                try {
                    ImageIO.write(bufferedFrame, "jpg", outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

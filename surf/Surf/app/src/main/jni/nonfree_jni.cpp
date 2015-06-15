/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/nonfree/nonfree.hpp>
#include <common.h>

/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Java source
 * file located at:
 *
 *   apps/samples/hello-jni/project/src/com/example/hellojni/HelloJni.java
 */
using namespace cv;
using namespace std;

extern "C"{
      JNIEXPORT double JNICALL
      Java_com_opencv_surf_SurfBaseJni_computeMatchingPoints
      (JNIEnv *env, jobject obj, jstring objectImgPath, jstring sceneImgPath)
      {
            //return env->NewStringUTF("Hello from JNI");
			const char* inCStrObjectImgPath = env->GetStringUTFChars(objectImgPath, 0);
			const char* inCStrSceneImgPath = env->GetStringUTFChars(sceneImgPath, 0);
		
          Mat img_object= imread( inCStrObjectImgPath, CV_LOAD_IMAGE_GRAYSCALE );
          Mat img_scene = imread( inCStrSceneImgPath, CV_LOAD_IMAGE_GRAYSCALE );

          if( !img_object.data || !img_scene.data )
          {
            // Can not load images from sdcard!
            return -1;
           }

          //-- Step 1: Detect the keypoints using SURF Detector
          int minHessian = 400;

          SurfFeatureDetector detector( minHessian );

          std::vector<KeyPoint> keypoints_object, keypoints_scene;

          detector.detect( img_object, keypoints_object );
          detector.detect( img_scene, keypoints_scene );

          //-- Step 2: Calculate descriptors (feature vectors)
          SurfDescriptorExtractor extractor;

          Mat descriptors_object, descriptors_scene;

          extractor.compute( img_object, keypoints_object, descriptors_object );
          extractor.compute( img_scene, keypoints_scene, descriptors_scene );

          //-- Step 3: Matching descriptor vectors using FLANN matcher
          FlannBasedMatcher matcher;
          std::vector< DMatch > matches;
          matcher.match( descriptors_object, descriptors_scene, matches );

          double max_dist = 0; double min_dist = 100;
		LOGI("Scene descriptors size:=%d",descriptors_scene.rows);
          //-- Quick calculation of max and min distances between keypoints
          for( int i = 0; i < descriptors_object.rows; i++ )
          { double dist = matches[i].distance;
            if( dist < min_dist ) min_dist = dist;
            if( dist > max_dist ) max_dist = dist;
          }
			//LOGI("Descriptors object rows:",descriptors_object.rows);
          //-- Draw only "good" matches (i.e. whose distance is less than 3*min_dist )
          std::vector< DMatch > good_matches;
          int goodMatches = 0;
			LOGI("Object descriptors size:=%d",descriptors_object.rows);
			LOGI("Total matches:=%d",matches.size());
			double score = 0;
          for( int i = 0; i < descriptors_object.rows; i++ )
          {
				score += matches[i].distance;
				if( matches[i].distance <= 3*min_dist )
				{ 
					goodMatches++;
				}
          }
		  score/=descriptors_object.rows;
		 
		LOGI("Number of good matches:=%d",goodMatches);
		  int percentage = 0;
		  percentage = (goodMatches*100)/descriptors_object.rows;
		   LOGI("Percentage:=%d",percentage);
            return score;
      }
}

cmake_minimum_required(VERSION 3.22.1)

project("imageprocessor")

# Set OpenCV path - UPDATED to match your folder name
set(OPENCV_SDK_PATH "C:/Users/RAKESH/AndroidStudioProjects/CameraImageProcessor/OpenCV-android-sdk/sdk")

# Include OpenCV headers
include_directories(${OPENCV_SDK_PATH}/native/jni/include)

# Add your source file
add_library(imageprocessor SHARED
        native-image-processor.cpp)

# Find required libraries
find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)

# Import OpenCV library
add_library(opencv_java4 SHARED IMPORTED)
set_target_properties(opencv_java4 PROPERTIES
        IMPORTED_LOCATION ${OPENCV_SDK_PATH}/native/libs/${ANDROID_ABI}/libopencv_java4.so)

# Link everything together
target_link_libraries(imageprocessor
        ${log-lib}
        ${jnigraphics-lib}
        opencv_java4)
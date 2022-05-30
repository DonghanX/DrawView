# Drawing [![](https://jitpack.io/v/DonghanX/Drawing.svg)](https://jitpack.io/#DonghanX/Drawing)

A simple Android view for drawing.

<img src="screenshots/demo_with_canvas_background.gif" width="50%">

## Setup

#### Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

#### Step 2. Add the dependency
```gradle
dependencies {
    implementation 'com.github.DonghanX:Drawing:1.0.0'
}
```

## Features
* Support multiple types of lines, including solid line, dashed line and Chisel Tip line.
* Eraser, Redo, Undo and ClearAll.
* Change the background of the Canvas by setting image resource or color.
* Keep track of the state after performing drawing actions.
* Save the Canvas as Bitmap.


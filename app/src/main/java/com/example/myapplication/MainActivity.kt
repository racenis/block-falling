package com.example.myapplication

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GestureDetectorCompat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var gLView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

        Log.d("AAA", "Starting")
        gLView = MyGLSurfaceView(this)
        setContentView(gLView)




        /*val benis: Button = findViewById(R.id.yes_button)
        benis.setOnClickListener {
            val bimage: ImageView = findViewById(R.id.indian_image)
            bimage.visibility = View.VISIBLE

            val btext: TextView = findViewById(R.id.r_u_gay_text)
            btext.visibility = View.GONE

            benis.visibility = View.GONE

        }*/
    }
}

private const val DEBUG_TAG = "Gestures"


class MyGLSurfaceView(context: Context) : GLSurfaceView(context), GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private val renderer: MyGLRenderer

    private lateinit var mDetector: GestureDetectorCompat

    private var dragged = false
    private var dragging = 0

    init {

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(3)

        renderer = MyGLRenderer()

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer)

        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY




        mDetector = GestureDetectorCompat(context, this)
        mDetector.setOnDoubleTapListener(this)

    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        return if (mDetector.onTouchEvent(e)) {
            true
        } else {
            super.onTouchEvent(e)
        }
    }

    override fun onDown(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onDown: $event")
        dragging = 0
        dragged = false
        return true
    }

    override fun onFling(
        event1: MotionEvent,
        event2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        //Log.d(DEBUG_TAG, "onFling: $event1 $event2")
        //Log.d("FLING", "FLINGAA: $velocityX $velocityY")
        if(abs(velocityX) > 5000 || abs(velocityY) > 5000) {
            if(abs(velocityX) > abs(velocityY)){
                if(velocityX > 0){
                    Log.d("FLING", "right")
                    // right
                    //Triangle.Tetromino.rotationX += 1
                    //if (Triangle.Tetromino.rotationX > 3) Triangle.Tetromino.rotationX = 0
                    Triangle.Tetromino.rotateRight()
                    requestRender()
                } else {
                    Log.d("FLING", "left")
                    // left
                    Triangle.Tetromino.rotateLeft()
                    //Triangle.Tetromino.rotationX -= 1
                    //if (Triangle.Tetromino.rotationX < 0) Triangle.Tetromino.rotationX = 3
                    requestRender()
                }
            } else {
                if(velocityY > 0){
                    Log.d("FLING", "down")
                    // down
                    Triangle.Tetromino.rotateDown()
                    //Triangle.Tetromino.rotationY += 1
                    //if (Triangle.Tetromino.rotationY > 3) Triangle.Tetromino.rotationY = 0
                    requestRender()
                } else {
                    Log.d("FLING", "up")
                    // up
                    Triangle.Tetromino.rotateUp()
                    //Triangle.Tetromino.rotationY -= 1
                    //if (Triangle.Tetromino.rotationY < 0) Triangle.Tetromino.rotationY = 3
                    requestRender()
                }
            }
        }
        return true
    }

    override fun onLongPress(event: MotionEvent) {
        Log.d(DEBUG_TAG, "onLongPress: $event")
    }

    override fun onScroll(
        event1: MotionEvent,
        event2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        //Log.d(DEBUG_TAG, "onScroll: $event1 $event2")
        Log.d(DEBUG_TAG, "onScroll: $dragging")
        dragging++
        if (/*!dragged &&*/ (dragging > 10) && (abs(distanceX) > 2 || abs(distanceY) > 2)) {
            Log.d(DEBUG_TAG, "OYOYOYOYOYOYOOOOYOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO")
            dragged = true
            dragging = -10
            if (abs(distanceX) > abs(distanceY)) {
                if (distanceX > 0) {
                    //Triangle.Tetromino.locationX--
                    Triangle.Tetromino.moveLeft()
                    requestRender()
                } else {
                    //Triangle.Tetromino.locationX++
                    Triangle.Tetromino.moveRight()
                    requestRender()
                }
            } else {
                if (distanceY > 0) {
                    //Triangle.Tetromino.locationY++
                    Triangle.Tetromino.moveUp()
                    requestRender()
                } else {
                    //Triangle.Tetromino.locationY--
                    Triangle.Tetromino.moveDown()
                    requestRender()
                }
            }
        }
        //Log.d(DEBUG_TAG, "onScroll: $distanceX $distanceY")
        return true
    }

    override fun onShowPress(event: MotionEvent) {
        Log.d(DEBUG_TAG, "onShowPress: $event")
    }

    override fun onSingleTapUp(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onSingleTapUp: $event")
        return true
    }

    override fun onDoubleTap(event: MotionEvent): Boolean {
        //Log.d(DEBUG_TAG, "onDoubleTap: $event")
        Triangle.Tetromino.dropDown()
        requestRender()
        return true
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: $event")
        return true
    }

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
        Log.d(DEBUG_TAG, "onSingleTapConfirmed: $event")
        return true
    }



}

class MyGLRenderer : GLSurfaceView.Renderer {
    private lateinit var mTriangle: Triangle
    //private lateinit var mSquare: Square

    // vPMatrix is an abbreviation for "Model View Projection Matrix"
    private val vPMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)



    override fun onSurfaceCreated(unused: GL10, config: EGLConfig) {
        // Set the background frame color
        GLES30.glClearColor(0.0f, 1.0f, 0.0f, 1.0f)

        GLES30.glEnable( GLES30.GL_DEPTH_TEST );
        GLES30.glDepthFunc( GLES30.GL_LEQUAL );
        GLES30.glDepthMask( true );

        GLES30.glEnable(GLES30.GL_CULL_FACE)

        // initialize a triangle
        mTriangle = Triangle()
        // initialize a square
        //mSquare = Square2()




    }

    override fun onDrawFrame(unused: GL10) {
        // Redraw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        mTriangle.draw(vPMatrix)
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    

}


fun loadShader(vertexCode: String, fragmentCode: String): Int {
    val vertex = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
    val fragment = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)

    GLES30.glShaderSource(vertex, vertexCode)
    GLES30.glCompileShader(vertex)
    Log.d("SHH", GLES30.glGetProgramInfoLog(vertex))
    GLES30.glShaderSource(fragment, fragmentCode)
    GLES30.glCompileShader(fragment)
    Log.d("SHH", GLES30.glGetProgramInfoLog(fragment))

    val shader = GLES30.glCreateProgram()

    GLES30.glAttachShader(shader, vertex)
    GLES30.glAttachShader(shader, fragment)
    GLES30.glLinkProgram(shader)
    Log.d("SHH", GLES30.glGetProgramInfoLog(shader))

    return shader
}




// number of coordinates per vertex in this array
const val COORDS_PER_VERTEX = 3
var triangleCoords = floatArrayOf(     // in counterclockwise order:
    0.0f, 0.622008459f, 0.0f,      // top
    -0.5f, -0.311004243f, 0.0f,    // bottom left
    0.5f, -0.311004243f, 0.0f      // bottom right
)

var triangleColors = floatArrayOf(
    1.0f, 0.0f, 0.0f, 1.0f,
    0.0f, 1.0f, 0.0f, 1.0f,
    0.0f, 0.0f, 1.0f, 1.0f,
)

class Triangle {

    object Field {
        val width: Int = 5
        val length: Int = 5
        val depth: Int = 7
        val fieldUnit: Float  = 0.15f
        val cornerX: Float  = 0.0f - ((fieldUnit * width.toFloat()) / 2.0f)
        val cornerY: Float  = 0.0f - ((fieldUnit * length.toFloat()) / 2.0f)
        val cornerZ: Float  = -1.0f
        var field = Array(width) { Array(length) { BooleanArray(depth) { false } } }

        fun pointIsInside(x: Int, y: Int, z: Int) = x >= 0 && y >= 0 && z >= 0 && x < width && y < length && z < depth
        fun pointIsCollide(x: Int, y: Int, z: Int) = field[x][y][z]
        fun fieldAppend(x: Int, y: Int, z: Int) { field[x][y][z] = true }

        init {
            /*field[0][0][0] = true
            field[4][0][0] = true
            field[0][4][0] = true
            field[4][4][0] = true

            field[0][0][1] = true
            field[4][0][1] = true
            field[0][4][1] = true
            field[4][4][1] = true

            field[0][0][2] = true
            field[4][0][2] = true
            field[0][4][2] = true
            field[4][4][2] = true




            field[2][2][0] = true*/
        }

        lateinit var vertices: FloatArray
        lateinit var colors: FloatArray
        lateinit var textures: FloatArray

        fun assembleVertexArrays() {
            Log.d("AAA", "Assembling field vertices")
            var verts: MutableList<Float> = mutableListOf()
            var colrs: MutableList<Float> = mutableListOf()

            for (i in 0 until width)
            for (j in 0 until length)
            for (k in 0 until depth){
                //Log.d("AAA", "Checking $i $j $k")
                if(field[i][j][k]){
                    Log.d("AAA", "Found $i $j $k")
                    val c_left: Float = cornerX + (fieldUnit * i.toFloat())
                    val c_right: Float = cornerX + (fieldUnit * (i+1).toFloat())
                    val c_lower: Float = cornerY + (fieldUnit * j.toFloat())
                    val c_upper: Float = cornerY + (fieldUnit * (j+1).toFloat())
                    val c_bottom: Float = cornerZ + (fieldUnit * k.toFloat())
                    val c_top: Float = cornerZ + (fieldUnit * (k+1).toFloat())
                    val col = {colrs.add(Random.nextFloat()); colrs.add(Random.nextFloat()); colrs.add(Random.nextFloat()); colrs.add(1.0f)}
                    val tlf = {verts.add(c_left); verts.add(c_upper); verts.add(c_top); col()}
                    val trf = {verts.add(c_right); verts.add(c_upper); verts.add(c_top); col()}
                    val blf = {verts.add(c_left); verts.add(c_lower); verts.add(c_top); col()}
                    val brf = {verts.add(c_right); verts.add(c_lower); verts.add(c_top); col()}
                    val tlb = {verts.add(c_left); verts.add(c_upper); verts.add(c_bottom); col()}
                    val trb = {verts.add(c_right); verts.add(c_upper); verts.add(c_bottom); col()}
                    val blb = {verts.add(c_left); verts.add(c_lower); verts.add(c_bottom); col()}
                    val brb = {verts.add(c_right); verts.add(c_lower); verts.add(c_bottom); col()}

                    // virsējais
                    tlf()
                    blf()
                    brf()
                    brf()
                    trf()
                    tlf()

                    // augšējais
                    trf()
                    trb()
                    tlb()
                    tlb()
                    tlf()
                    trf()

                    // apakšējais

                    brf()
                    blf()
                    blb()
                    blb()
                    brb()
                    brf()

                    // kreisais
                    tlf()
                    tlb()
                    blb()
                    blb()
                    blf()
                    tlf()

                    // labais
                    trf()
                    brf()
                    brb()
                    brb()
                    trb()
                    trf()
                }
            }

            Log.d("AAA", "Done assembling")
            vertices = verts.toFloatArray()
            colors = colrs.toFloatArray()
        }
    }

    object Tetromino {
        data class TetrominoSegment(val x: Int, val y: Int){
            var rotated = FloatArray(4)
            fun applyRotation(matrix: FloatArray){
                val r = FloatArray(4)
                r[0] = x.toFloat()
                r[1] = y.toFloat()
                r[2] = 0.0f
                r[3] = 1.0f

                Matrix.multiplyMV(rotated, 0, matrix, 0, r, 0)
            }

            val transfX
                get() = locationX + rotated[0].roundToInt()

            val transfY
                get() = locationY + rotated[1].roundToInt()

            val transfZ
                get() = locationZ + rotated[2].roundToInt()

            /*
            val transfX
                get() = locationX + when(Pair(rotationX, rotationY)){
                    Pair(0, 0) -> x
                    Pair(1, 0) -> y
                    Pair(2, 0) -> -x
                    Pair(3, 0) -> -y

                    Pair(0, 1) -> x
                    Pair(1, 1) -> y
                    Pair(2, 1) -> -x
                    Pair(3, 1) -> -y

                    Pair(0, 2) -> x
                    Pair(1, 2) -> y
                    Pair(2, 2) -> -x
                    Pair(3, 2) -> -y

                    Pair(0, 3) -> x
                    Pair(1, 3) -> y
                    Pair(2, 3) -> -x
                    Pair(3, 3) -> -y

                    else -> x
                }
            val transfY
                get() = locationY + when(Pair(rotationX, rotationY)){
                    Pair(0, 0) -> y
                    Pair(1, 0) -> x
                    Pair(2, 0) -> -y
                    Pair(3, 0) -> -x

                    Pair(0, 1) -> 0
                    Pair(1, 1) -> 0
                    Pair(2, 1) -> 0
                    Pair(3, 1) -> 0

                    Pair(0, 2) -> -y
                    Pair(1, 2) -> -x
                    Pair(2, 2) -> y
                    Pair(3, 2) -> x

                    Pair(0, 3) -> 0
                    Pair(1, 3) -> 0
                    Pair(2, 3) -> 0
                    Pair(3, 3) -> 0
                    else -> y
                }
            val transfZ
                get() = locationZ + when(Pair(rotationX, rotationY)){
                    Pair(0, 0) -> 0
                    Pair(1, 0) -> 0
                    Pair(2, 0) -> 0
                    Pair(3, 0) -> 0

                    Pair(0, 1) -> y
                    Pair(1, 1) -> x
                    Pair(2, 1) -> -y
                    Pair(3, 1) -> -x

                    Pair(0, 2) -> 0
                    Pair(1, 2) -> 0
                    Pair(2, 2) -> 0
                    Pair(3, 2) -> 0

                    Pair(0, 3) -> -y
                    Pair(1, 3) -> -x
                    Pair(2, 3) -> y
                    Pair(3, 3) -> x
                    else -> 0
                }
             */
        }
        val TetrominoType = listOf(
            listOf(
                TetrominoSegment(0, 0),
                TetrominoSegment(1, 0),
                TetrominoSegment(-1, 0),
                TetrominoSegment(1, 1)
            ),
            listOf(
                TetrominoSegment(0, 0),
                TetrominoSegment(1, 0),
                TetrominoSegment(0, 1),
                TetrominoSegment(1, 1)
            ),
            listOf(
                TetrominoSegment(0, 0),
                TetrominoSegment(1, 0),
                TetrominoSegment(-1, 0),
                TetrominoSegment(0, 1)
            ),
            listOf(
                TetrominoSegment(0, 0),
                TetrominoSegment(1, 0),
                TetrominoSegment(-1, 0),
                TetrominoSegment(2, 0)
            )
        )

        var current = TetrominoType[0]
        var rotationX: Int = 0
        var rotationY: Int = 0
        var locationX: Int = 2
        var locationY: Int = 2
        var locationZ: Int = 4

        fun reset() {
            locationX = 2
            locationY = 2
            locationZ = 4
            Matrix.setIdentityM(rotationMatrix, 0)
            current = TetrominoType.random()
        }


        var rotationMatrix = FloatArray(16)
        init {
            Matrix.setIdentityM(rotationMatrix, 0)
        }

        fun rotateUp() {
            val oldMatrix = rotationMatrix.copyOf()
            Matrix.rotateM(rotationMatrix, 0, 90.0f, 0.0f, 1.0f, 0.0f)
            if(!isLocationRotationValid()) rotationMatrix = oldMatrix
        }
        fun rotateDown() {
            val oldMatrix = rotationMatrix.copyOf()
            Matrix.rotateM(rotationMatrix, 0, -90.0f, 0.0f, 1.0f, 0.0f)
            if(!isLocationRotationValid()) rotationMatrix = oldMatrix
        }
        fun rotateLeft() {
            val oldMatrix = rotationMatrix.copyOf()
            Matrix.rotateM(rotationMatrix, 0, 90.0f, 0.0f, 0.0f, 1.0f)
            if(!isLocationRotationValid()) rotationMatrix = oldMatrix
        }
        fun rotateRight() {
            val oldMatrix = rotationMatrix.copyOf()
            Matrix.rotateM(rotationMatrix, 0, -90.0f, 0.0f, 0.0f, 1.0f)
            if(!isLocationRotationValid()) rotationMatrix = oldMatrix
        }
        fun moveLeft() {
            val oldLocation = locationX
            locationX--
            if(!isLocationRotationValid()) locationX = oldLocation
        }
        fun moveRight() {
            val oldLocation = locationX
            locationX++
            if(!isLocationRotationValid()) locationX = oldLocation
        }
        fun moveUp() {
            val oldLocation = locationY
            locationY++
            if(!isLocationRotationValid()) locationY = oldLocation
        }
        fun moveDown() {
            val oldLocation = locationY
            locationY--
            if(!isLocationRotationValid()) locationY = oldLocation
        }
        fun dropDown() {
            val oldLocation = locationZ
            locationZ--
            if(!isLocationRotationValid()) {
                locationZ = oldLocation
                for (t in current) Field.fieldAppend(t.transfX, t.transfY, t.transfZ)
                reset()
            }
        }



        fun isLocationRotationValid(): Boolean {
            for (t in current) {
                t.applyRotation(rotationMatrix)
                if (
                    !Field.pointIsInside(t.transfX, t.transfY, t.transfZ) ||
                    Field.pointIsCollide(t.transfX, t.transfY, t.transfZ)
                ) return false
            }
            return true
        }

        lateinit var vertices: FloatArray

        fun assembleVertexArrays() {
            Log.d("AAA", "Assembling tetromino vertices")
            var verts: MutableList<Float> = mutableListOf()

            for (t in current){
                t.applyRotation(rotationMatrix)

                val c_left: Float = Field.cornerX + (Field.fieldUnit * t.transfX.toFloat())
                val c_right: Float = Field.cornerX + (Field.fieldUnit * (t.transfX+1).toFloat())
                val c_lower: Float = Field.cornerY + (Field.fieldUnit * t.transfY.toFloat())
                val c_upper: Float = Field.cornerY + (Field.fieldUnit * (t.transfY+1).toFloat())
                val c_bottom: Float = Field.cornerZ + (Field.fieldUnit * t.transfZ.toFloat())
                val c_top: Float = Field.cornerZ + (Field.fieldUnit * (t.transfZ+1).toFloat())
                val tlf = {verts.add(c_left); verts.add(c_upper); verts.add(c_top)}
                val trf = {verts.add(c_right); verts.add(c_upper); verts.add(c_top)}
                val blf = {verts.add(c_left); verts.add(c_lower); verts.add(c_top)}
                val brf = {verts.add(c_right); verts.add(c_lower); verts.add(c_top)}
                val tlb = {verts.add(c_left); verts.add(c_upper); verts.add(c_bottom)}
                val trb = {verts.add(c_right); verts.add(c_upper); verts.add(c_bottom)}
                val blb = {verts.add(c_left); verts.add(c_lower); verts.add(c_bottom)}
                val brb = {verts.add(c_right); verts.add(c_lower); verts.add(c_bottom)}

                tlf()
                trf()
                trf()
                brf()
                brf()
                blf()
                blf()
                tlf()

                tlb()
                trb()
                trb()
                brb()
                brb()
                blb()
                blb()
                tlb()

                tlf()
                tlb()
                trf()
                trb()
                brf()
                brb()
                blf()
                blb()

            }

            Log.d("AAA", "Done assembling")
            vertices = verts.toFloatArray()
        }

    }

    private val vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "attribute vec4 veColor;" +
                "varying vec4 vertColor;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "  vertColor = veColor;" +
                "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "varying vec4 vertColor;" +
                "void main() {" +
                "  gl_FragColor = vertColor;" +
                "}"

    private val lineVertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"

    private val lineFragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"


    // Use to access and set the view transformation
    private var vPMatrixHandle: Int = 0




    // Set color with red, green, blue and alpha (opacity) values
    val color = floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f)




    private var mProgram: Int = loadShader(vertexShaderCode, fragmentShaderCode)
    private var lineShader: Int = loadShader(lineVertexShaderCode, lineFragmentShaderCode)

    var positionHandle: Int = 0
    var colorHandle: Int = 0
    var mColorHandle: Int = 0

    val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
    val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    val colorVertexStride: Int = 4 * 4 // 4 bytes per vertex

    fun draw(mvpMatrix: FloatArray) {
        Log.d("AAA", "Starting the draw")
        Field.assembleVertexArrays()
        Tetromino.assembleVertexArrays()

        val triangles = Field.vertices
        val colors = Field.colors
        val lines = Tetromino.vertices

        Log.d("AAA", "Making buffers")
        var vertexBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(triangles.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(triangles)
                    position(0)
                }
            }

        var vertexColorBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(colors.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(colors)
                    position(0)
                }
            }

        var lineVertexBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(lines.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(lines)
                    position(0)
                }
            }

        Log.d("AAA", "Done making buffers")


        GLES30.glUseProgram(mProgram)

        positionHandle = GLES30.glGetAttribLocation(mProgram, "vPosition")
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glVertexAttribPointer(
            positionHandle,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            vertexBuffer
        )

        colorHandle = GLES30.glGetAttribLocation(mProgram, "veColor")
        GLES30.glEnableVertexAttribArray(colorHandle)
        GLES30.glVertexAttribPointer(
            colorHandle,
            4, // krāsas kā 4 komponenšu vektors
            GLES30.GL_FLOAT,
            false,
            colorVertexStride,
            vertexColorBuffer
        )


        mColorHandle = GLES30.glGetUniformLocation(mProgram, "vColor").also { colorHandle ->
            GLES30.glUniform4fv(colorHandle, 1, color, 0)
        }

        vPMatrixHandle = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix")
        GLES30.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, triangles.size / 3)

        GLES30.glDisableVertexAttribArray(colorHandle)
        GLES30.glDisableVertexAttribArray(positionHandle)







        GLES30.glUseProgram(lineShader)

        positionHandle = GLES30.glGetAttribLocation(lineShader, "vPosition")
        GLES30.glEnableVertexAttribArray(positionHandle)
        GLES30.glVertexAttribPointer(
            positionHandle,
            COORDS_PER_VERTEX,
            GLES30.GL_FLOAT,
            false,
            vertexStride,
            lineVertexBuffer
        )

        mColorHandle = GLES30.glGetUniformLocation(lineShader, "vColor").also { colorHandle ->
            GLES30.glUniform4fv(colorHandle, 1, color, 0)
        }

        vPMatrixHandle = GLES30.glGetUniformLocation(lineShader, "uMVPMatrix")
        GLES30.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

        GLES30.glDrawArrays(GLES30.GL_LINES, 0, lines.size / 3)

        GLES30.glDisableVertexAttribArray(positionHandle)

    }


    }


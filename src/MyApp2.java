import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetShader;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.util.glu.GLU;

class MyApp2 {
    // Entry point for the application
    public static void main(String[] args) {
        new MyApp2();
    }

    // Setup variables
    private final String WINDOW_TITLE = "The Quad: glDrawArrays";
    private final int WIDTH = 320;
    private final int HEIGHT = 240;
    // Quad variables
    private int vaoId = 0;
    private int vboId = 0;
    private int vertexCount = 0;

    public MyApp2() {
        // Initialize OpenGL (Display)
        this.setupOpenGL();

        this.setupQuad();

        while (!Display.isCloseRequested()) {
            // Do a single loop (logic/render)
            this.loopCycle();

            // Force a maximum FPS of about 60
            Display.sync(60);
            // Let the CPU synchronize with the GPU if GPU is tagging behind
            Display.update();
        }

        // Destroy OpenGL (Display)
        this.destroyOpenGL();
    }

    public void setupOpenGL() {
        // Setup an OpenGL context with API version 3.2
        try {
            PixelFormat pixelFormat = new PixelFormat();
            ContextAttribs contextAtrributes = new ContextAttribs(3, 2)
                .withForwardCompatible(true)
                .withProfileCore(true);

            Display.setDisplayMode(new DisplayMode(WIDTH, HEIGHT));
            Display.setTitle(WINDOW_TITLE);
            Display.create(pixelFormat, contextAtrributes);

            GL11.glViewport(0, 0, WIDTH, HEIGHT);
        } catch (LWJGLException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // we'll need a program to render anything with OpenGL 3.2
        int program = glCreateProgram();

        // create a vertex shader
        int vertexShaderId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShaderId, toByteBuffer(
                                                    "#version 150 core\n" +
                                                    "in vec3 vVertex;\n" +
                                                    "void main() {\n" +
                                                    "  gl_Position = vec4(vVertex.x, vVertex.y, vVertex.z, 1.0);\n" +
                                                    "}"));
        glCompileShader(vertexShaderId);
        if (glGetShaderi(vertexShaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            printLogInfo(vertexShaderId);
            System.exit(-1);
        }

        // create a fragment shader
        int fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShaderId, toByteBuffer(
                                                      "#version 150 core\n" +
                                                      "out vec4 color;\n" +
                                                      "void main() {\n" +
                                                      "  color = vec4(1.0);\n" +
                                                      "}"));
        glCompileShader(fragmentShaderId);
        if (glGetShader(fragmentShaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            printLogInfo(fragmentShaderId);
            System.exit(-1);
        }

        // attach to program
        glAttachShader(program, vertexShaderId);
        glAttachShader(program, fragmentShaderId);
        glLinkProgram(program);
        glUseProgram(program);
        //<<<<<<< NEW STUFF

        // Setup an XNA like background color
        GL11.glClearColor(0.4f, 0.6f, 0.9f, 0f);

        // Map the internal OpenGL coordinate system to the entire screen
        GL11.glViewport(0, 0, WIDTH, HEIGHT);

        this.exitOnGLError("Error in setupOpenGL");
    }

    public void setupQuad() {       
        // OpenGL expects vertices to be defined counter clockwise by default
        float[] vertices = {
            // Left bottom triangle
            -0.5f, 0.5f, 0f,
            -0.5f, -0.5f, 0f,
            0.5f, -0.5f, 0f,
            // Right top triangle
            0.5f, -0.5f, 0f,
            0.5f, 0.5f, 0f,
            -0.5f, 0.5f, 0f
        };
        // Sending data to OpenGL requires the usage of (flipped) byte buffers
        FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
        verticesBuffer.put(vertices);
        verticesBuffer.flip();

        vertexCount = 6;

        // Create a new Vertex Array Object in memory and select it (bind)
        // A VAO can have up to 16 attributes (VBO's) assigned to it by default
        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // Create a new Vertex Buffer Object in memory and select it (bind)
        // A VBO is a collection of Vectors which in this case resemble the location of each vertex.
        vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
        // Put the VBO in the attributes list at index 0
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        // Deselect (bind to 0) the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        // Deselect (bind to 0) the VAO
        GL30.glBindVertexArray(0);

        this.exitOnGLError("Error in setupQuad");
    }

    public void loopCycle() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        // Bind to the VAO that has all the information about the quad vertices
        GL30.glBindVertexArray(vaoId);
        GL20.glEnableVertexAttribArray(0);

        // Draw the vertices
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vertexCount);

        // Put everything back to default (deselect)
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);

        this.exitOnGLError("Error in loopCycle");
    }

    public void destroyOpenGL() {       
        // Disable the VBO index from the VAO attributes list
        GL20.glDisableVertexAttribArray(0);

        // Delete the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vboId);

        // Delete the VAO
        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoId);

        Display.destroy();
    }

    public void exitOnGLError(String errorMessage) {
        int errorValue = GL11.glGetError();

        if (errorValue != GL11.GL_NO_ERROR) {
            String errorString = GLU.gluErrorString(errorValue);
            System.err.println("ERROR - " + errorMessage + ": " + errorString);

            if (Display.isCreated()) Display.destroy();
            System.exit(-1);
        }
    }

    //>>>>>>> NEW STUFF
    private ByteBuffer toByteBuffer(final String data) {
        byte[] vertexShaderData = data.getBytes(Charset.forName("ISO-8859-1"));
        ByteBuffer vertexShader = BufferUtils.createByteBuffer(vertexShaderData.length);
        vertexShader.put(vertexShaderData);
        vertexShader.flip();
        return vertexShader;
    }

    private void printLogInfo(final int obj) {
        ByteBuffer infoLog = BufferUtils.createByteBuffer(2048);
        IntBuffer lengthBuffer = BufferUtils.createIntBuffer(1);
        glGetShaderInfoLog(obj, lengthBuffer, infoLog);

        byte[] infoBytes = new byte[lengthBuffer.get()];
        infoLog.get(infoBytes);
        if (infoBytes.length == 0) {
            return;
        }
        System.err.println(new String(infoBytes, Charset.forName("ISO-8859-1")));
    }
}

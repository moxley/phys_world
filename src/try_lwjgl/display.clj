(ns try-lwjgl.display
  ;;(:gen-class)
  (:import [org.lwjgl.opengl Display DisplayMode GL11 GL15 GL20]
           [org.lwjgl.input Mouse Keyboard]
           [org.lwjgl.util.glu GLU]
           [org.lwjgl.util.vector Vector4f]
           [org.lwjgl BufferUtils]
           [java.awt Font]
           [java.nio.charset Charset]
           [utility Camera EulerCamera EulerCamera$Builder Model OBJLoader])
  (:require [try-lwjgl.logic :as logic]
            [clojure.java.io :as io]))

(def WIDTH 800)
(def HEIGHT 600)
(def program (atom nil))
(def camera (atom nil))

(defn exitOnGLError [errorMessage]
  (let [errorValue (GL11/glGetError)]
    (if (not (=  errorValue GL11/GL_NO_ERROR))
      (let [errorString (GLU/gluErrorString errorValue)]
        (.println System/err (str  "ERROR - " errorMessage ": " errorString))
        (when (Display/isCreated) (Display/destroy))
        (System/exit -1)))))

(defn printLogInfo [obj]
  (let [infoLog (BufferUtils/createByteBuffer 2048)
        lengthBuffer (BufferUtils/createIntBuffer 1)
        _ (GL20/glGetShaderInfoLog obj lengthBuffer infoLog)
        infoBytes (byte-array (.get lengthBuffer))]
    (.get infoLog infoBytes)
    (if (= (count infoBytes) 0)
      nil ; return
      (.println System/err (String. infoBytes (Charset/forName "ISO-8859-1"))))))

(defn toByteBuffer [data]
  (let [vertexShaderData (.getBytes data (Charset/forName "ISO-8859-1"))
        vertexShader (BufferUtils/createByteBuffer (count vertexShaderData))]
    (.put vertexShader vertexShaderData)
    (.flip vertexShader)
    vertexShader))

(defn setup-vertex-shader [program]
  (let [vertexShaderId (GL20/glCreateShader GL20/GL_VERTEX_SHADER)]
    (GL20/glShaderSource vertexShaderId (toByteBuffer (slurp (io/file (io/resource "try_lwjgl/shader.vs")))))
    (GL20/glCompileShader vertexShaderId)
    (when (= (GL20/glGetShader vertexShaderId GL20/GL_COMPILE_STATUS) GL11/GL_FALSE)
      (printLogInfo vertexShaderId)
      (System/exit -1))
    (GL20/glAttachShader program vertexShaderId)))

(defn setup-fragment-shader [program]
  (let [fragmentShaderId (GL20/glCreateShader GL20/GL_FRAGMENT_SHADER)]
    (GL20/glShaderSource fragmentShaderId (toByteBuffer (slurp (io/file (io/resource "try_lwjgl/shader.fs")))))
    (GL20/glCompileShader fragmentShaderId)
    (when (= (GL20/glGetShader fragmentShaderId GL20/GL_COMPILE_STATUS) GL11/GL_FALSE) 
      (printLogInfo fragmentShaderId)
      (System/exit -1))
    (GL20/glAttachShader program fragmentShaderId)))

(defn setup-opengl [width height title]
  (Display/setDisplayMode (DisplayMode. width height))
  (Display/setTitle title)
  (Display/create)
  (println "OpenGL Version:" (GL11/glGetString GL11/GL_VERSION))

  (swap! program (fn [_] (GL20/glCreateProgram)))

  (GLU/gluPerspective (float 45.0) ;; fovy
                      (/ (float WIDTH) (float HEIGHT)) ;; aspect
                      (float 0.1)     ;; zNear
                      (float 100.0))  ;; zFar

  ;; Create and attach shaders to program
  (setup-vertex-shader @program)
  (setup-fragment-shader @program)

  (GL20/glLinkProgram @program)
  (GL20/glValidateProgram @program)
  (exitOnGLError "Error in setupOpenGL"))

(defn setup-camera []
  (let [builder (EulerCamera$Builder.)
        _ (doto builder
            (.setAspectRatio
             (/ (float (Display/getWidth)) (Display/getHeight)))
            (.setRotation (float -1.12) (float 0.16) (float 0))
            (.setPosition (float -1.38) (float 1.36) (float 7.95))
            (.setFieldOfView 60))
        c (.build builder)]
    (swap! camera (fn [_] c))
    (.applyOptimalStates c)
    (.applyPerspectiveMatrix c)))

(defmacro do-shape [type & commands]
  `(do
    (GL11/glBegin ~type)
    ~@commands
    (GL11/glEnd)))

(defmacro drawing-object [& commands]
  `(do
     (GL11/glPushMatrix)
     ~@commands
     (GL11/glPopMatrix)))

(defn draw-rectangle []
  (drawing-object
   ;; x, y, z
   (GL11/glTranslatef 1.5 0.0 0.0)

   ;; glRotatef
   ;;   arg0: angle
   ;;   arg1: x axis (left to right)
   ;;   arg2: y axis (down to up)
   ;;   arg3: z axis (front to back)
   (GL11/glRotatef @logic/angle 1.0 0.0 0.0)

   (GL11/glColor3f 0.5 0.5 1.0)

   (do-shape GL11/GL_QUADS
             (GL11/glVertex3f -1.0  1.0 0.0)
             (GL11/glVertex3f  1.0  1.0 0.0)
             (GL11/glVertex3f  1.0 -1.0 0.0)
             (GL11/glVertex3f -1.0 -1.0 0.0))))

(def VERTEXT-WIDTH 0.05)

(defn draw-vertices []
  (drawing-object

   ;;(GL11/glTranslatef -1.5 0.0 0.0)

   (GL11/glScaled 4.0 4.0 4.0)

   ;; x-axis (red)
   (GL11/glColor3f 1.0 0.0 0.0)
   (do-shape GL11/GL_QUADS
             (GL11/glVertex3f -1.0 0.0 0.0)
             (GL11/glVertex3f  1.0 0.0 0.0)
             (GL11/glVertex3f  1.0 VERTEXT-WIDTH 0.0)
             (GL11/glVertex3f -1.0 VERTEXT-WIDTH 0.0))

   ;; y-axis (green)
   (GL11/glColor3f 0.0 1.0 0.0)
   (do-shape GL11/GL_QUADS
             (GL11/glVertex3f 0.0 -1.0 0.0)
             (GL11/glVertex3f 0.0  1.0 0.0)
             (GL11/glVertex3f 0.0  1.0 VERTEXT-WIDTH)
             (GL11/glVertex3f 0.0 -1.0 VERTEXT-WIDTH))

   ;; z-axis (blue)
   (GL11/glColor3f 0.0 0.0 1.0)
   (do-shape GL11/GL_QUADS
             (GL11/glVertex3f 0.0 0.0 -1.0)
             (GL11/glVertex3f 0.0 0.0  1.0)
             (GL11/glVertex3f 0.0 VERTEXT-WIDTH  1.0)
             (GL11/glVertex3f 0.0 VERTEXT-WIDTH -1.0))))

(defn draw-container-cube []
  (drawing-object
   (GL11/glScaled 10.0 10.0 10.0)
   (GL11/glLineWidth 2.5)
   (GL11/glColor3f 1.0 0.0 0.0)
   (do-shape GL11/GL_LINES
             (GL11/glVertex3f 0 0 0)
             (GL11/glVertex3f 1 1 1))

   (do-shape GL11/GL_LINES
             (GL11/glVertex3f 1 1 -1)
             (GL11/glVertex3f -1 1 -1))
   (do-shape GL11/GL_LINES
             (GL11/glVertex3f -1 1 -1)
             (GL11/glVertex3f -1 -1 -1))
   (do-shape GL11/GL_LINES
             (GL11/glVertex3f -1 -1 -1)
             (GL11/glVertex3f 1 -1 -1))
   (do-shape GL11/GL_LINES
             (GL11/glVertex3f 1 -1 -1)
             (GL11/glVertex3f 1 1 -1))

   (do-shape GL11/GL_LINES
             (GL11/glVertex3f 1 1 1)
             (GL11/glVertex3f -1 1 1))
   (do-shape GL11/GL_LINES
             (GL11/glVertex3f -1 1 1)
             (GL11/glVertex3f -1 -1 1))
   (do-shape GL11/GL_LINES
             (GL11/glVertex3f -1 -1 1)
             (GL11/glVertex3f 1 -1 1))
   (do-shape GL11/GL_LINES
             (GL11/glVertex3f 1 -1 1)
             (GL11/glVertex3f 1 1 1))

   (do-shape GL11/GL_LINES
             (GL11/glVertex3f -1 -1 -1)
             (GL11/glVertex3f -1 -1 1)
             (GL11/glVertex3f -1 1 1)
             (GL11/glVertex3f -1 1 -1))

   (do-shape GL11/GL_LINES
             (GL11/glVertex3f 1 -1 -1)
             (GL11/glVertex3f 1 -1 1)
             (GL11/glVertex3f 1 1 1)
             (GL11/glVertex3f 1 1 -1))))

(defn translate-position [position vector]
  (let [position-buf (Vector4f. (float (position 0))
                                (float (position 1))
                                (float (position 2))
                                (float 1))]
    (.translate position-buf
                (float (vector 0))
                (float (vector 1))
                (float (vector 2))
                (float 0))
    [(.getX position-buf)
     (.getY position-buf)
     (.getZ position-buf)
     (float 1)]))

(defn handle-input []
  (.processMouse @camera 1 80 -80)
  (.processKeyboard @camera 16 1 1 1)
  (cond
   (Mouse/isButtonDown 0) (Mouse/setGrabbed true)
   (Mouse/isButtonDown 1) (Mouse/setGrabbed false)))

(defn draw []
  (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
  (GL11/glLoadIdentity)
  (.applyTranslations @camera)
        
  (GL20/glUseProgram @program)

  (let [[look-x look-y look-z] (logic/translate-with-direction @logic/player-position @logic/player-direction)
        [pos-x pos-y pos-z] @logic/player-position]
    (GLU/gluLookAt pos-x pos-y pos-z
                   look-x look-y look-z
                   (float 0.0) (float 1.0) (float 0.0)))

  (draw-vertices)
  (draw-container-cube)
  (draw-rectangle)

  (GL20/glUseProgram 0)
  (exitOnGLError "Error in draw"))

(defn iteration [delta]
  (draw)
  (handle-input))

(defn init []
  (setup-opengl WIDTH HEIGHT "alpha")
  (setup-camera))

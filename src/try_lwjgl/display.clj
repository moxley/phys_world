(ns try-lwjgl.display
  ;;(:gen-class)
  (:import [org.lwjgl.opengl Display DisplayMode GL11 GL15 GL20]
           [org.lwjgl.input Mouse Keyboard]
           [org.lwjgl.util.glu GLU]
           [org.lwjgl.util.vector Vector4f]
           [org.lwjgl BufferUtils]
           [org.lwjgl.util.glu Sphere]
           [java.awt Font]
           [java.nio.charset Charset]
           [utility Camera EulerCamera EulerCamera$Builder Model OBJLoader]
           [org.newdawn.slick Color]
           [org.newdawn.slick.opengl Texture TextureLoader]
           [org.newdawn.slick.util ResourceLoader])
  (:require [try-lwjgl.logic :as logic]
            [try-lwjgl.physics :as physics]
            [clojure.java.io :as io]))

(def WIDTH 800)
(def HEIGHT 600)
(def program (atom nil))
(def camera (atom nil))
(def texture (atom nil))

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
  (Display/setVSyncEnabled true)
  (Display/setTitle title)
  (Display/create)
  (println "OpenGL Version:" (GL11/glGetString GL11/GL_VERSION))

  (swap! program (fn [_] (GL20/glCreateProgram)))

  (GLU/gluPerspective (float 45.0) ;; fovy
                      (/ (float WIDTH) (float HEIGHT)) ;; aspect
                      (float 0.1)     ;; zNear
                      (float 100.0))  ;; zFar

  ;;
  ;; Texture support
  ;;
  (GL11/glEnable GL11/GL_TEXTURE_2D)
  ;; enable alpha blending
  (GL11/glEnable GL11/GL_BLEND)
  (GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)
  
  (GL11/glShadeModel GL11/GL_SMOOTH)
  (GL11/glClearColor (float 0.0) (float 0.0) (float 0.0) (float 0.0))
  (GL11/glClearDepth (float 1.0))
  (GL11/glEnable GL11/GL_DEPTH_TEST)
  (GL11/glDepthFunc GL11/GL_LEQUAL)
  (GL11/glHint GL11/GL_PERSPECTIVE_CORRECTION_HINT GL11/GL_NICEST)

  ;; Create and attach shaders to program
  (setup-vertex-shader @program)
  (setup-fragment-shader @program)

  (GL20/glLinkProgram @program)
  (GL20/glValidateProgram @program)
  (exitOnGLError "Error in setupOpenGL")

  (swap! texture (fn [_]  (TextureLoader/getTexture "JPG" (ResourceLoader/getResourceAsStream "try_lwjgl/mahogany.jpg")))))

(defn build-camera []
  (.build
   (doto (EulerCamera$Builder.)
     (.setAspectRatio
      (/ (float WIDTH) (float HEIGHT)))
     (.setRotation (float -1.12) (float 0.16) (float 0))
     (.setPosition (float -1.38) (float 1.36) (float 7.95))
     (.setFieldOfView 60))))

(defn setup-camera []
  (doto (swap! camera (fn [_] (build-camera)))
    (.applyOptimalStates)
    (.applyPerspectiveMatrix)))

(defmacro do-shape [type & commands]
  `(do
    (GL11/glBegin ~type)
    ~@commands
    (GL11/glEnd)))

(defmacro with-pushed-matrix [& commands]
  `(do
     (GL11/glPushMatrix)
     ~@commands
     (GL11/glPopMatrix)))

(defn draw-textured-panel []
  (.bind Color/white)
  (.bind @texture)

  (do-shape GL11/GL_QUADS
            (GL11/glTexCoord2f 1 0)
            (GL11/glVertex3f 1 0 0)
            (GL11/glTexCoord2f 0 0)
            (GL11/glVertex3f 0 0 0)
            (GL11/glTexCoord2f 0 1)
            (GL11/glVertex3f 0 1 0)
            (GL11/glTexCoord2f 1 1)
            (GL11/glVertex3f 1 1 0)))

(defn draw-rectangle []
  (with-pushed-matrix
   (GL11/glColor3f 1 1 1)
   (GL11/glTranslatef 0 -10 0)
   (GL11/glRotatef 90 1 0 0)
   (draw-textured-panel)))

(def AXIS-WIDTH 0.05)

(defmacro with-shader-program [& commands]
  `(do
     (GL20/glUseProgram @program)
     ~@commands
     (GL20/glUseProgram 0)))

(defn draw-axes []
  (with-shader-program
    ;; Height markers
    (doseq [n (range 10)]
      (with-pushed-matrix
        ;; x-axis (red)
        (GL11/glColor3f 1 0 0)
        (GL11/glTranslatef 0 n 0)
        (do-shape GL11/GL_LINE_STRIP
                  (GL11/glVertex3f -10 0 0)
                  (GL11/glVertex3f 10 0 0))))


    (with-pushed-matrix

     (GL11/glScaled 4.0 4.0 4.0)

     ;; x-axis (red)
     (GL11/glColor3f 1.0 0.0 0.0)
     (do-shape GL11/GL_QUADS
               (GL11/glVertex3f -1.0 0.0 0.0)
               (GL11/glVertex3f  1.0 0.0 0.0)
               (GL11/glVertex3f  1.0 AXIS-WIDTH 0.0)
               (GL11/glVertex3f -1.0 AXIS-WIDTH 0.0))

     ;; y-axis (green)
     (GL11/glColor3f 0.0 1.0 0.0)
     (do-shape GL11/GL_QUADS
               (GL11/glVertex3f 0.0 -1.0 0.0)
               (GL11/glVertex3f 0.0  1.0 0.0)
               (GL11/glVertex3f 0.0  1.0 AXIS-WIDTH)
               (GL11/glVertex3f 0.0 -1.0 AXIS-WIDTH))

     ;; z-axis (blue)
     (GL11/glColor3f 0.0 0.0 1.0)
     (do-shape GL11/GL_QUADS
               (GL11/glVertex3f 0.0 0.0 -1.0)
               (GL11/glVertex3f 0.0 0.0  1.0)
               (GL11/glVertex3f 0.0 AXIS-WIDTH  1.0)
               (GL11/glVertex3f 0.0 AXIS-WIDTH -1.0)))))

(defn draw-container-cube []
  (with-shader-program
    (with-pushed-matrix
     (GL11/glScaled 10.0 10.0 10.0)
     (GL11/glLineWidth 2.5)
     (GL11/glColor3f 1.0 0.0 0.0)

     ;; Back wall
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

     ;; Front wall
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

     ;; Left wall
     (do-shape GL11/GL_LINES
               (GL11/glVertex3f -1 -1 -1)
               (GL11/glVertex3f -1 -1 1)
               (GL11/glVertex3f -1 1 1)
               (GL11/glVertex3f -1 1 -1))

     ;; Right wall
     (do-shape GL11/GL_LINES
               (GL11/glVertex3f 1 -1 -1)
               (GL11/glVertex3f 1 -1 1)
               (GL11/glVertex3f 1 1 1)
               (GL11/glVertex3f 1 1 -1)))))

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

(defn draw-block [draw-panel]
    ;; face
  (draw-panel)

  ;; back
  (with-pushed-matrix
   (GL11/glTranslatef 0 0 -1)
   (draw-panel))

  ;; ;; left
  (with-pushed-matrix
   (GL11/glRotatef 90 0 1 0)
   (draw-panel))

  ;; right
  (with-pushed-matrix
   (GL11/glTranslatef 1 0 0)
   (GL11/glRotatef 90 0 1 0)
   (draw-panel))

  ;; back
  (with-pushed-matrix
   (GL11/glRotatef -90 1 0 0)
   (draw-panel))

  ;; ;; top
  (with-pushed-matrix
   (GL11/glTranslatef 0 1 0)
   (GL11/glRotatef -90 1 0 0)
   (draw-panel)))

(defn draw-textured-block []
  (draw-block draw-textured-panel))

(defn draw-stairs []
  (doseq [side [0 1 2 3]]
    (doseq [up [0 1 2]]
      (with-pushed-matrix
       (GL11/glTranslatef side up (* -1.0 up))
       (draw-textured-block)))))

(defn draw-floor []
  (doseq [x (range -10 10)
          z (range -10 10)]
    (with-pushed-matrix
     (GL11/glTranslatef x -10 z)
     (GL11/glRotatef 90 1 0 0)
     (draw-textured-panel))))

(def world (atom nil))

(defn world-and-objects []
  (if @world
    @world
    (swap! world (fn [_] (physics/build-world-with-objects)))))

(defn reset-ball []
  (println "reset-ball")
  (let [w (world-and-objects)
        ball (:ball w)]
    ;; This doesn't work
    ;; (set! (.x ball) 0.0)
    ;; (set! (.y ball) 35.0)
    ;; (set! (.z ball) 0.0)
    ))

(defn draw-sphere []
  (let [sphere (Sphere.)
        w (world-and-objects)
        ball (:ball w)
        ground (:ground w)
        world (:world w)
        pos (physics/get-position ball)]
    (with-pushed-matrix
     (with-shader-program
       (GL11/glTranslatef (pos 0) (pos 1) (pos 2))
       (.setDrawStyle sphere GLU/GLU_SILHOUETTE)
       (GL11/glColor4f 0 0.6 0 1)
       (.draw sphere 1.0 30 30)))))

(defn draw-models []
  (draw-axes)
  (draw-container-cube)
  (draw-stairs)
  (draw-floor)
  (draw-sphere))

(defn draw []
  (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
  (GL11/glLoadIdentity)
  (.applyTranslations @camera)

  (draw-models)
  
  (exitOnGLError "Error in draw"))

(defn handle-input []
  (.processMouse @camera 1 80 -80)
  (.processKeyboard @camera 16 1 1 1)
  (loop []
      (when (Keyboard/next)
        (when (Keyboard/getEventKeyState)
          (let [key (Keyboard/getEventKey)]
            (cond
             (= key Keyboard/KEY_B) (reset-ball)
              :else nil)))
        (recur)))
  (cond
   (Mouse/isButtonDown 0) (Mouse/setGrabbed false)
   (Mouse/isButtonDown 1) (Mouse/setGrabbed false)
   (Mouse/isButtonDown 2) (println "Mouse button 2 down")))

(defn iteration [delta]
  (let [world (:world (world-and-objects))]
    (.stepSimulation world (* delta 1000.0)))
  (draw)
  (handle-input))

(defn init []
  (setup-opengl WIDTH HEIGHT "alpha")
  (setup-camera))

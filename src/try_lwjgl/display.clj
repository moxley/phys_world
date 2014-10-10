(ns try-lwjgl.display
  ;;(:gen-class)
  (:import [org.lwjgl.opengl Display DisplayMode GL11]
           [org.lwjgl.input Mouse Keyboard]
           [org.lwjgl.util.glu GLU]
           [org.lwjgl.util.vector Vector4f]
           [org.lwjgl.util.glu Sphere]
           [java.awt Font]
           [utility Camera EulerCamera EulerCamera$Builder Model OBJLoader])
  (:require [try-lwjgl.logic :as logic]
            [try-lwjgl.physics :as physics]
            [try-lwjgl.shader :as shader]
            [try-lwjgl.model.axes :as axes]
            [try-lwjgl.model.wood-block :as wood-block]
            [try-lwjgl.model.textured-panel :as textured-panel]
            [try-lwjgl.display.util :as util]
            [clojure.java.io :as io]))

(def WIDTH 800)
(def HEIGHT 600)
(def camera (atom nil))

(defn setup-opengl [width height title]
  (Display/setDisplayMode (DisplayMode. width height))
  (Display/setVSyncEnabled true)
  (Display/setTitle title)
  (Display/create)
  (println "OpenGL Version:" (GL11/glGetString GL11/GL_VERSION))

  (GLU/gluPerspective (float 45.0) ;; fovy
                      (/ (float WIDTH) (float HEIGHT)) ;; aspect
                      (float 0.1)     ;; zNear
                      (float 100.0))  ;; zFar

  (textured-panel/setup)

  (GL11/glShadeModel GL11/GL_SMOOTH)
  (GL11/glClearColor (float 0.0) (float 0.0) (float 0.0) (float 0.0))
  (GL11/glClearDepth (float 1.0))
  (GL11/glEnable GL11/GL_DEPTH_TEST)
  (GL11/glDepthFunc GL11/GL_LEQUAL)
  (GL11/glHint GL11/GL_PERSPECTIVE_CORRECTION_HINT GL11/GL_NICEST)

  (shader/setup)

  (util/exit-on-gl-error "Error in setupOpenGL"))

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

(defn draw-container-cube []
  (shader/with-program
    (util/with-pushed-matrix
     (GL11/glScaled 10.0 10.0 10.0)
     (GL11/glLineWidth 2.5)
     (GL11/glColor3f 1.0 0.0 0.0)

     ;; Back wall
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f 1 1 -1)
               (GL11/glVertex3f -1 1 -1))
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f -1 1 -1)
               (GL11/glVertex3f -1 -1 -1))
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f -1 -1 -1)
               (GL11/glVertex3f 1 -1 -1))
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f 1 -1 -1)
               (GL11/glVertex3f 1 1 -1))

     ;; Front wall
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f 1 1 1)
               (GL11/glVertex3f -1 1 1))
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f -1 1 1)
               (GL11/glVertex3f -1 -1 1))
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f -1 -1 1)
               (GL11/glVertex3f 1 -1 1))
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f 1 -1 1)
               (GL11/glVertex3f 1 1 1))

     ;; Left wall
     (util/do-shape GL11/GL_LINES
               (GL11/glVertex3f -1 -1 -1)
               (GL11/glVertex3f -1 -1 1)
               (GL11/glVertex3f -1 1 1)
               (GL11/glVertex3f -1 1 -1))

     ;; Right wall
     (util/do-shape GL11/GL_LINES
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

(defn draw-stairs []
  (doseq [side [0 1 2 3]]
    (doseq [up [0 1 2]]
      (util/with-pushed-matrix
       (GL11/glTranslatef side up (* -1.0 up))
       (wood-block/draw)))))

(defn draw-floor []
  (doseq [x (range -10 10)
          z (range -10 10)]
    (util/with-pushed-matrix
     (GL11/glTranslatef x -10 z)
     (GL11/glRotatef 90 1 0 0)
     (textured-panel/draw))))

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
    (util/with-pushed-matrix
     (shader/with-program
       (GL11/glTranslatef (pos 0) (pos 1) (pos 2))
       (.setDrawStyle sphere GLU/GLU_SILHOUETTE)
       (GL11/glColor4f 0 0.6 0 1)
       (.draw sphere 1.0 30 30)))))

(defn draw-models []
  (axes/draw)
  (draw-container-cube)
  (draw-stairs)
  (draw-floor)
  (draw-sphere))

(defn draw []
  (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
  (GL11/glLoadIdentity)
  (.applyTranslations @camera)

  (draw-models)
  
  (util/exit-on-gl-error "Error in draw"))

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

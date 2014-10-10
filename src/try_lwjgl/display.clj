(ns try-lwjgl.display
  (:import [org.lwjgl.opengl Display DisplayMode GL11]
           [org.lwjgl.input Mouse Keyboard]
           [org.lwjgl.util.glu GLU]
           [org.lwjgl.util.vector Vector4f]
           [java.awt Font]
           [utility Camera EulerCamera EulerCamera$Builder Model OBJLoader])
  (:require [try-lwjgl.logic :as logic]
            [try-lwjgl.physics :as physics]
            [try-lwjgl.shader :as shader]
            [try-lwjgl.model.axes :as axes]
            [try-lwjgl.model.textured-panel :as textured-panel]
            [try-lwjgl.model.stairs :as stairs]
            [try-lwjgl.model.floor :as floor]
            [try-lwjgl.model.sphere :as sphere]
            [try-lwjgl.model.container-cube :as container-cube]
            [try-lwjgl.display.util :as util]
            [clojure.java.io :as io]))

(def WIDTH 800)
(def HEIGHT 600)
(def camera (atom nil))
(def ball (atom nil))

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

(defn draw-models []
  (axes/draw)
  (container-cube/draw)
  (stairs/draw)
  (floor/draw)
  (sphere/draw @ball))

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
             (= key Keyboard/KEY_B) (println "Pressed B")
              :else nil)))
        (recur)))
  (cond
   (Mouse/isButtonDown 0) (Mouse/setGrabbed false)
   (Mouse/isButtonDown 1) (Mouse/setGrabbed false)
   (Mouse/isButtonDown 2) (println "Mouse button 2 down")))

(defn iteration [delta]
  (let [world (:world (physics/world-and-objects))]
    (.stepSimulation world (* delta 1000.0)))
  (draw)
  (handle-input))

(defn init []
  (setup-opengl WIDTH HEIGHT "alpha")
  (let [w (physics/world-and-objects)
        world (:world w)]
    (swap! ball (fn [_] (sphere/create world 1.0 [0 5 0]))))
  (setup-camera))

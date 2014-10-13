(ns try-lwjgl.display
  (:import [org.lwjgl.opengl Display DisplayMode GL11]
           [org.lwjgl.input Mouse Keyboard]
           [org.lwjgl.util.glu GLU]
           [java.awt Font]
           [utility Camera EulerCamera EulerCamera$Builder Model OBJLoader]
           [javax.vecmath Matrix4f Quat4f Vector3f])
  (:require [try-lwjgl.logic :as logic]
            [try-lwjgl.physics :as physics]
            [try-lwjgl.camera :as model.camera]
            [try-lwjgl.shader :as shader]
            [try-lwjgl.model.axes :as axes]
            [try-lwjgl.model.grid :as grid]
            [try-lwjgl.model.textured-panel :as textured-panel]
            [try-lwjgl.model.stairs :as stairs]
            [try-lwjgl.model.player :as model.player]
            [try-lwjgl.model.ground :as model.ground]
            [try-lwjgl.model.ball :as model.ball]
            [try-lwjgl.model.container-cube :as container-cube]
            [try-lwjgl.display.util :as util]
            [try-lwjgl.math :as math]
            [clojure.java.io :as io]))

(def WIDTH 800)
(def HEIGHT 600)
(def camera (atom nil))
(def player (atom nil))
(def world (atom nil))
(def ball (atom nil))
(def ground (atom nil))

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

;; (defn build-camera []
;;   (.build
;;    (doto (EulerCamera$Builder.)
;;      (.setAspectRatio
;;       (/ (float WIDTH) (float HEIGHT)))
;;      (.setRotation (float -1.12) (float 0.16) (float 0))
;;      (.setPosition (float -1.38) (float -9) (float 7.95))
;;      (.setFieldOfView 60))))

;; (defn setup-camera []
;;   (doto (swap! camera (fn [_] (build-camera)))
;;     (.applyOptimalStates)
;;     (.applyPerspectiveMatrix)))

(defn draw-models []
  (axes/draw)
  ;(grid/draw)
  (container-cube/draw)
  (stairs/draw)
  (model.player/draw @player)
  (model.ground/draw @ground)
  (model.ball/draw @ball))

(def pointer (atom nil))

(defn get-pointer []
  (or @pointer
      (swap! pointer (fn [_] (model.ball/create @world 0.25 [0 0 0])))))


(defn calc-pointer-loc [camera-pos orientation]
  (let [[cx cy cz] camera-pos
        [pitch yaw roll] orientation
        factor (float 1)
        normal [(math/sin yaw) (* -1 (math/sin pitch)) (* -1 (math/cos yaw))]
        scaled (math/scale normal factor)
        translated (math/add camera-pos scaled)]
    translated))

(defn set-pointer []
  (let [pointer (get-pointer)
        camera-pos (model.camera/position)
        orientation (model.camera/orientation)
        new-pos (calc-pointer-loc camera-pos orientation)]
    (physics/reset-body (:phys pointer) new-pos)))

(defn draw-pointer []
  (model.ball/draw (get-pointer)))

(defn move-player []
  (let [p @player
        camera-pos (model.camera/position)]
    (physics/reset-body (:phys p) camera-pos)))

(defn draw []
  (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
  (GL11/glLoadIdentity)

  ;;(.applyTranslations @camera)
  (model.camera/point @player)

  (draw-models)

  ;; (draw-pointer)
  ;; (set-pointer)
  ;;(move-player)

  (util/exit-on-gl-error "Error in draw"))

(defn handle-input [delta]
  (model.player/logic delta @player)
  (loop []
      (when (Keyboard/next)
        (when (Keyboard/getEventKeyState)
          (let [key (Keyboard/getEventKey)]
            (cond
             (= key Keyboard/KEY_C) (do
                                      (let [c @camera
                                            camera-pos [(.x c) (.y c) (.z c)]
                                            orientation [(.pitch c) (.yaw c) (.roll c)]]
                                        (println "camera-pos:" camera-pos ", orientation:" orientation)))
             (= key Keyboard/KEY_1) (set-pointer)
             :else nil)))
        (recur)))
  (cond
   (Mouse/isButtonDown 0) (do (println "Mouse button 0 down") (Mouse/setGrabbed true))
   (Mouse/isButtonDown 1) (do (println "Mouse button 1 down") (Mouse/setGrabbed false))
   (Mouse/isButtonDown 2) (println "Mouse button 2 down")))

(defn iteration [delta]
  (.stepSimulation @world (* delta 1000.0))
  ;;(.processMouse @camera 1 80 -80)
  ;;(.processKeyboard @camera 16 1 1 1)

  (handle-input delta)
  (.stepSimulation @world 0)
  (draw))

(defn init []
  (setup-opengl WIDTH HEIGHT "alpha")
  (swap! world (fn [_] (physics/build-world)))
  (swap! ball (fn [_] (model.ball/create @world 1.0 [0 5 0])))
  (swap! ground (fn [_] (model.ground/create @world [0 -10 0])))
  (swap! player (fn [_] (model.player/create @world [-2 0 10])))
  ;;(setup-camera)
  (model.camera/setup WIDTH HEIGHT))

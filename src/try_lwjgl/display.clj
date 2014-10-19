(ns try-lwjgl.display
  (:import [org.lwjgl.opengl Display DisplayMode GL11])
  (:require [try-lwjgl.models :as models]
            [try-lwjgl.camera :as model.camera]
            [try-lwjgl.shader :as shader]
            [try-lwjgl.model.textured-panel :as textured-panel]
            [try-lwjgl.display.util :as util]
            [clojure.java.io :as io]))

(def WIDTH 800)
(def HEIGHT 600)

(defn setup-opengl [width height title]
  (Display/setDisplayMode (DisplayMode. width height))
  (Display/setVSyncEnabled true)
  (Display/setTitle title)
  (Display/create)
  (println "OpenGL Version:" (GL11/glGetString GL11/GL_VERSION))

  (model.camera/apply-perspective WIDTH HEIGHT)

  (textured-panel/setup)

  (GL11/glShadeModel GL11/GL_SMOOTH)
  (GL11/glClearColor (float 0.0) (float 0.0) (float 0.0) (float 0.0))
  (GL11/glClearDepth (float 1.0))
  (GL11/glEnable GL11/GL_DEPTH_TEST)
  (GL11/glDepthFunc GL11/GL_LEQUAL)
  (GL11/glHint GL11/GL_PERSPECTIVE_CORRECTION_HINT GL11/GL_NICEST)

  (shader/setup)

  (util/exit-on-gl-error "Error in setupOpenGL"))

(defn draw []
  (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))
  (GL11/glLoadIdentity)

  (model.camera/point @models/player)
  (models/draw)
  (util/exit-on-gl-error "Error in draw"))

(defn init []
  (setup-opengl WIDTH HEIGHT "alpha")
  (models/init))

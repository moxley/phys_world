(ns phys-world.display
  (:import [org.lwjgl.opengl Display DisplayMode GL11])
  (:require [phys-world.models :as models]
            [phys-world.camera :as model.camera]
            [phys-world.shader :as shader]
            [phys-world.model.textured-panel :as textured-panel]
            [phys-world.display.util :as util]
            [clojure.java.io :as io]))

(def WIDTH 1024)
(def HEIGHT 768)

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
  (model.camera/draw-crosshairs @models/player)
  (util/exit-on-gl-error "Error in draw"))

(defn init []
  (setup-opengl WIDTH HEIGHT "alpha"))

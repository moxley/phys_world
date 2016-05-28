(ns phys-world.camera
  (:import [org.lwjgl.opengl Display DisplayMode GL11 GLContext ARBDepthClamp]
           [org.lwjgl.input Mouse Keyboard]
           [org.lwjgl.util.glu GLU]
           [javax.vecmath Matrix4f Quat4f Vector3f Vector4f]
           [utility Camera EulerCamera EulerCamera$Builder Model OBJLoader])
  (:require [phys-world.logic :as logic]
            [phys-world.physics :as physics]
            [phys-world.shader :as shader]
            [phys-world.display.util :as util]
            [phys-world.math :as math]
            [clojure.java.io :as io]))

(def fov 45)
(def zNear (float 0.1))
(def zFar (float 100))
(def camera (atom nil))

(defn draw-crosshairs [player]
  (let [phys-player (:phys player)
        pos (physics/get-position phys-player)
        [px py pz]       pos
        orientation (deref (:orientation player))
        [pitch yaw roll] orientation
        len 0.05]

    (GL11/glMatrixMode GL11/GL_PROJECTION)
    (util/with-pushed-matrix
      (GL11/glLoadIdentity)
      (GL11/glOrtho (float -1)
                    (float 1)
                    (float (* -1 (/ 600 800)))
                    (float (/ 600 800))
                    (float -1)
                    (float 1))

      (GL11/glMatrixMode GL11/GL_MODELVIEW)
      (util/with-pushed-matrix
        (GL11/glLoadIdentity)

        (util/with-pushed-attrib GL11/GL_DEPTH_TEST
          (GL11/glDisable GL11/GL_DEPTH_TEST)

          (util/with-pushed-attrib GL11/GL_DEPTH_WRITEMASK
            (GL11/glDepthMask false)

            (shader/with-program
              ;; x-axis (red)
              (GL11/glColor3f 1.0 0.0 0.0)
              (util/do-shape GL11/GL_LINES
                             (GL11/glVertex3f (* -1 len) 0 0)
                             (GL11/glVertex3f len 0 0))

              ;; y-axis (green)
              (GL11/glColor3f 0.0 1.0 0.0)
              (util/do-shape GL11/GL_LINES
                             (GL11/glVertex3f 0 (* -1 len) 0)
                             (GL11/glVertex3f 0 len 0)))))

        (GL11/glMatrixMode GL11/GL_PROJECTION))
      (GL11/glMatrixMode GL11/GL_MODELVIEW))))

(defn apply-optimal-states []
  (when (.GL_ARB_depth_clamp (GLContext/getCapabilities))
    (GL11/glEnable ARBDepthClamp/GL_DEPTH_CLAMP)))

(defn apply-perspective [width height]
  (apply-optimal-states)
  (let [aspect-ratio (/ (float width) (float height))]
      (GL11/glPushAttrib GL11/GL_TRANSFORM_BIT)
      (GL11/glMatrixMode GL11/GL_PROJECTION)
      (GL11/glLoadIdentity)
      (GLU/gluPerspective fov aspect-ratio zNear zFar)
      (GL11/glPopAttrib)))

(defn point [player]
  (let [phys (player :phys)
        player-pos (physics/get-position phys)
        [pitch yaw roll] (deref (:orientation player))
        [x y z] player-pos]
    (GL11/glPushAttrib GL11/GL_TRANSFORM_BIT)
    (GL11/glMatrixMode GL11/GL_MODELVIEW)
    (GL11/glRotatef pitch 1 0 0)
    (GL11/glRotatef (+ 180 yaw) 0 1 0)
    (GL11/glRotatef roll 0 0 1)
    (GL11/glTranslatef (* -1 x) (* -1  y) (* -1 z))
    (GL11/glPopAttrib)))

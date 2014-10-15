(ns try-lwjgl.camera
  (:import [org.lwjgl.opengl Display DisplayMode GL11 GLContext ARBDepthClamp]
           [org.lwjgl.input Mouse Keyboard]
           [org.lwjgl.util.glu GLU]
           [javax.vecmath Matrix4f Quat4f Vector3f Vector4f]
           [utility Camera EulerCamera EulerCamera$Builder Model OBJLoader])
  (:require [try-lwjgl.logic :as logic]
            [try-lwjgl.physics :as physics]
            [try-lwjgl.shader :as shader]
            [try-lwjgl.display.util :as util]
            [try-lwjgl.math :as math]
            [clojure.java.io :as io]))

(def fov 90)
(def zNear (float 0.3))
(def zFar 100)
(def camera (atom nil))

(defn apply-optimal-states []
  (when (.GL_ARB_depth_clamp (GLContext/getCapabilities))
    (GL11/glEnable ARBDepthClamp/GL_DEPTH_CLAMP)))

(defn apply-perspective-matrix [width height]
  (let [aspect-ratio (/ (float width) (float height))]
      (GL11/glPushAttrib GL11/GL_TRANSFORM_BIT)
      (GL11/glMatrixMode GL11/GL_PROJECTION)
      (GL11/glLoadIdentity)
      (GLU/gluPerspective fov aspect-ratio zNear zFar)
      (GL11/glPopAttrib)))

(defn set-aspect-ratio [])
(defn set-rotation [])

(defn build-camera [width height]
  (.build
   (doto (EulerCamera$Builder.)
     (.setAspectRatio
      (/ (float width) (float height)))
     (.setRotation (float 0) (float 0) (float 0))
     (.setPosition (float 0) (float 0) (float 10))
     (.setFieldOfView 60))))

(defn setup [width height]
  (apply-optimal-states)
  (apply-perspective-matrix width height))

(defn point [player]
  (let [phys (player :phys)
        player-pos (physics/get-position phys)
        [pitch yaw roll] (deref (:orientation player))
        [x y z] player-pos]
    (GL11/glPushAttrib GL11/GL_TRANSFORM_BIT)
    (GL11/glMatrixMode GL11/GL_MODELVIEW)
    (GL11/glRotatef pitch 1 0 0)
    (GL11/glRotatef yaw 0 1 0)
    (GL11/glRotatef roll 0 0 1)
    (GL11/glTranslatef (* -1 x) (* -1  y) (* -1 z))
    (GL11/glPopAttrib)))

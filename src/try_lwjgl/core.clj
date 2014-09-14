(ns try-lwjgl.core
  ;;(:gen-class)
  (:import [org.lwjgl.opengl Display DisplayMode GL11]
           [org.lwjgl.util.glu GLU]
           [org.lwjgl LWJGLException Sys]
           [java.awt Font]
           [org.newdawn.slick Color TrueTypeFont])
  (:require [try-lwjgl.logic :as logic]))

(def WIDTH 640)
(def HEIGHT 480)

(defn init-window
  [width height title]
  (def globals (ref {:width width
                     :height height
                     :title title
                     :angle 0.0
                     :last-time (System/currentTimeMillis)}))
  (Display/setDisplayMode (DisplayMode. width height))
  (Display/setVSyncEnabled true)
  (Display/setTitle title)
  (Display/create))

(defn init-gl []
  (GL11/glViewport 0 0 WIDTH HEIGHT)
  (GL11/glMatrixMode GL11/GL_PROJECTION)
  (GL11/glLoadIdentity)
  (GLU/gluPerspective (float 45.0) ;; fovy
                      (/ (float WIDTH) (float HEIGHT)) ;; aspect
                      (float 0.1)     ;; zNear
                      (float 100.0))  ;; zFar
  (GL11/glMatrixMode GL11/GL_MODELVIEW)
  (GL11/glLoadIdentity)

  (GL11/glShadeModel GL11/GL_SMOOTH)
  (GL11/glClearColor (float 0.0) (float 0.0) (float 0.0) (float 0.0))
  (GL11/glClearDepth (float 1.0))
  (GL11/glEnable GL11/GL_DEPTH_TEST)
  (GL11/glDepthFunc GL11/GL_LEQUAL)
  (GL11/glHint GL11/GL_PERSPECTIVE_CORRECTION_HINT GL11/GL_NICEST))

(defn draw-triangle []
  (GL11/glLoadIdentity)
  (GL11/glTranslatef -1.5 0.0 -6.0)
  (GL11/glRotatef @logic/angle 0.0 1.0 0.0)

  (GL11/glBegin GL11/GL_TRIANGLES)
  (GL11/glColor3f 1.0 0.0 0.0)
  (GL11/glVertex3f 0.0 1.0 0.0)
  (GL11/glColor3f 0.0 1.0 0.0)
  (GL11/glVertex3f -1.0 -1.0 0.0)
  (GL11/glColor3f 0.0 0.0 1.0)
  (GL11/glVertex3f 1.0 -1.0 0.0)
  (GL11/glEnd))

(defn draw-rectangle []
  (GL11/glLoadIdentity)
  (GL11/glTranslatef 1.5 0.0 -6.0)
  ;; glRotatef
  ;;   arg0: angle
  ;;   arg1: x axis (left to right)
  ;;   arg2: y axis (down to up)
  ;;   arg3: z axis (front to back)
  (GL11/glRotatef @logic/angle 1.0 0.0 0.0)

  (GL11/glColor3f 0.5 0.5 1.0)
  (GL11/glBegin GL11/GL_QUADS)
  (GL11/glVertex3f -1.0 1.0 0.0)
  (GL11/glVertex3f 1.0 1.0 0.0)
  (GL11/glVertex3f 1.0 -1.0 0.0)
  (GL11/glVertex3f -1.0 -1.0 0.0)
  (GL11/glEnd))

(defn draw-vertices []
  (GL11/glLoadIdentity)

  ;; x-axis
  (GL11/glColor3f 0.5 0.5 1.0)
  (GL11/glBegin GL11/GL_QUADS)
  (GL11/glVertex3f -1.0 0.0 0.0)
  (GL11/glVertex3f 1.0 0.0 0.0)
  (GL11/glVertex3f 1.0 0.1 0.0)
  (GL11/glVertex3f -1.0 0.1 0.0)
  (GL11/glEnd)

  ;; y-axis
  ;;(GL11/glVertex3f 0.0 1.0 0.0)
  )

(defn draw []
  (GL11/glClear (bit-or GL11/GL_COLOR_BUFFER_BIT GL11/GL_DEPTH_BUFFER_BIT))

  ;;(draw-vertices)

  ;;(draw-triangle)
  (draw-rectangle)
  )

(def last-frame-time (atom (Sys/getTime)))

(defn get-delta []
  (let [time (/ (* (Sys/getTime) 1000) (Sys/getTimerResolution))
        delta (int (- time @last-frame-time))]
    (swap! last-frame-time (fn [lft t] t) time)
    delta))

(defn update [delta]
  (logic/update delta))

(defn run
  []
  (init-window 800 600 "alpha")
  (init-gl)
  (while (not (Display/isCloseRequested))
    (update (get-delta))
    (draw)
    (Display/update)
    (Display/sync 60))
  (Display/destroy))

(defn -main
  []
  (println "Try LWJGL")
  (run))

(ns try-lwjgl.physics
  (:import [com.bulletphysics.collision.broadphase BroadphaseInterface DbvtBroadphase]
           [com.bulletphysics.collision.dispatch CollisionConfiguration CollisionDispatcher CollisionObject DefaultCollisionConfiguration]
           [com.bulletphysics.collision.shapes CollisionShape SphereShape CapsuleShape StaticPlaneShape]
           [com.bulletphysics.dynamics DiscreteDynamicsWorld DynamicsWorld RigidBody RigidBodyConstructionInfo]
           [com.bulletphysics.dynamics.constraintsolver ConstraintSolver SequentialImpulseConstraintSolver]
           [com.bulletphysics.linearmath DefaultMotionState MotionState Transform]
           [org.lwjgl.util.glu GLU Sphere]
           [javax.vecmath Matrix4f Quat4f Vector3f Vector4f AxisAngle4f])
  (:require [try-lwjgl.math :as math]))

(def GRAVITY 10)

(defn build-world []
  (let [broadphase (DbvtBroadphase.)
        collisionConfiguration (DefaultCollisionConfiguration.)
        dispatcher (CollisionDispatcher. collisionConfiguration)
        solver     (SequentialImpulseConstraintSolver.)
        world      (DiscreteDynamicsWorld. dispatcher broadphase solver collisionConfiguration)]
    (.setGravity world (math/jvec3f 0 (* -1 GRAVITY) 0))
    world))

(defn build-ground [position]
  (let [normal (math/jvec3f 0 1 0) ; Direction plane is facing
        plane-constant 0.0       ; Padding thickness above plane
        groundShape (StaticPlaneShape. normal plane-constant)
        groundMotionState (DefaultMotionState. (Transform. (math/jmatrix4f
                                                            (Quat4f. 0 0 0 1)
                                                            (apply math/jvec3f position)
                                                            (float 1))))
        groundBodyConstructionInfo (RigidBodyConstructionInfo. 0 groundMotionState groundShape (math/jvec3f 0 0 0))
        _ (set! (.restitution groundBodyConstructionInfo) 0.25)]
    (RigidBody. groundBodyConstructionInfo)))

(defn build-ball [radius position]
  (let [ballShape (SphereShape. radius)
        p (vec (map #(float %) position))
        default-ball-transform (Transform. (Matrix4f. (Quat4f. 0 0 0 1)
                                                      ;; Starting
                                                      ;; position
                                                      (apply math/jvec3f p)
                                                      (float 1)))
        ballMotionState (DefaultMotionState. default-ball-transform)
        ballInertia (math/jvec3f 0 0 0)
        _ (.calculateLocalInertia ballShape 2.5 ballInertia)
        ballConstructionInfo (RigidBodyConstructionInfo. 2.5 ballMotionState ballShape ballInertia)
        _ (set! (.restitution ballConstructionInfo) 0.5)
        _ (set! (.angularDamping ballConstructionInfo) 0.95)
        ball (RigidBody. ballConstructionInfo)
        _ (.setActivationState ball CollisionObject/DISABLE_DEACTIVATION)]
    ball))

(defn build-player [radius position]
  (let [radius radius
        ballShape (SphereShape. radius)
        p (vec (map #(float %) position))
        default-ball-transform (Transform. (Matrix4f. (Quat4f. 0 0 0 1)
                                                      ;; Starting
                                                      ;; position
                                                      (apply math/jvec3f p)
                                                      (float 1)))
        ballMotionState (DefaultMotionState. default-ball-transform)
        ballInertia (math/jvec3f 0 0 0)
        _ (.calculateLocalInertia ballShape 2.5 ballInertia)
        ballConstructionInfo (RigidBodyConstructionInfo. 2.5 ballMotionState ballShape ballInertia)
        _ (set! (.linearDamping ballConstructionInfo) 0.1)
        ;;_ (set! (.additionalDamping ballConstructionInfo) true)
        _ (set! (.restitution ballConstructionInfo) 0.5)
        _ (set! (.angularDamping ballConstructionInfo) 0.95)
        ball (RigidBody. ballConstructionInfo)
        _ (.setActivationState ball CollisionObject/DISABLE_DEACTIVATION)]
    ball))

(defn get-position [body]
  (let [position (.origin (.getWorldTransform (.getMotionState body) (Transform.)))]
    [(.x position) (.y position) (.z position)]))

(defn axis-angle-4f [x y z a]
  (AxisAngle4f. x y z a))

(defn reset-body [body position]
  (let [default-transform (Transform. (Matrix4f. (Quat4f. 0 0 0 1)
                                                 (apply math/jvec3f position)
                                                 (float 1)))]
    (.setCenterOfMassTransform body default-transform)
    (.setAngularVelocity body (math/jvec3f 0 0 0))
    (.setLinearVelocity body (math/jvec3f 0 0 0))))

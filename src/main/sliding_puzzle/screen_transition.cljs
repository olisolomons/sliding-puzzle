(ns sliding-puzzle.screen-transition
  (:require
   [quil.core :as q]
   [sliding-puzzle.sketch-functions :refer [add-derived-state draw-state
                                            key-pressed mouse-dragged mouse-pressed
                                            update-state]]
   [sliding-puzzle.easing-functions :as easing-functions]))

(def animation-duration 600)

(defn mk-state [from to direction]
  (add-derived-state
   {:type :screen-transition
    :canvas-size (:canvas-size from)
    :from from
    :to to
    :animation-start (q/millis)
    :direction (case direction
                 :left -1
                 :right 1
                 (throw (ex-info "Invalid direction"
                                 {:direction direction
                                  :allowed-directions #{:left :right}})))}))

(defmethod add-derived-state :screen-transition
  [{:keys [canvas-size from to] :as state}]
  (assoc state
         :from
         (add-derived-state
          (assoc from
                 :canvas-size canvas-size))
         :to
         (add-derived-state
          (assoc to
                 :canvas-size canvas-size))))

(defmethod mouse-dragged :screen-transition
  [state]
  state)

(defmethod update-state :screen-transition
  [{:keys [animation-start to] :as state}]
  (if (and animation-start
           (> (q/millis) (+ animation-start animation-duration)))
    to
    state))

(defmethod draw-state :screen-transition
  [{:keys [canvas-size from to animation-start direction]}]
  (let [shift (* direction
                 (easing-functions/ease-in-out-cubic
                  (/ (- (q/millis) animation-start) animation-duration)))]
    (q/with-translation
      [(* canvas-size (- 0 shift)) 0]
      (draw-state from))
    (q/with-translation
      [(* canvas-size (- direction shift)) 0]
      (draw-state to))))

(defmethod mouse-pressed :screen-transition
  [state _event]
  state)

(defmethod key-pressed :screen-transition
  [state _event]
  state)


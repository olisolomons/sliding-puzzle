(ns sliding-puzzle.win-screen
  (:require
   [quil.core :as q]
   [sliding-puzzle.buttons :as buttons]
   [sliding-puzzle.easing-functions :as easing-functions]
   [sliding-puzzle.screen-transition :as screen-transition]
   [sliding-puzzle.sketch-functions :refer [add-derived-state draw-state
                                            key-pressed mouse-dragged mouse-pressed
                                            update-state]]))

(def button-width 0.25)
(def buttom-height 0.1)
(def button-margin 0.04)

(def margin 20)

(def animation-duration 600)

(defn mk-state [game-state]
  (add-derived-state
   {:type :win-screen
    :game-state game-state
    :animation-start (q/millis)
    :canvas-size (:canvas-size game-state)}))

(defmethod add-derived-state :win-screen
  [{:keys [canvas-size game-state] :as state}]
  (let [p #(* canvas-size %)
        width-height (- (* 0.8 canvas-size) (* 2 margin))]
    (assoc state
           :buttons
           (remove
            nil?
            [(when (:next-level game-state)
               {:rect [(+ (p 0.5) (* width-height 0.5) (- (p button-width)) (- (p button-margin)))
                       (+ (p 0.5) (* width-height 0.5) (p (* buttom-height -1)) (- (p button-margin)))
                       (p button-width) (p buttom-height)]
                :fn (fn [state]
                      (screen-transition/mk-state state (:next-level game-state) :right))
                :text "Next Level"
                :text-size (* canvas-size 0.04)})
             {:rect [(+ (p 0.5) (* width-height -0.5) (p button-margin))
                     (+ (p 0.5) (* width-height 0.5) (p (* buttom-height -1)) (- (p button-margin)))
                     (p button-width) (p buttom-height)]
              :fn (fn [state] (screen-transition/mk-state
                               state
                               {:type :main-menu
                                :canvas-size (:canvas-size state)}
                               :left))
              :text "Main Menu"
              :text-size (* canvas-size 0.04)}])
           :game-state
           (add-derived-state
            (assoc game-state
                   :canvas-size canvas-size)))))

(defmethod update-state :win-screen
  [{:keys [animation-start] :as state}]
  (if (and animation-start
           (> (q/millis) (+ animation-start animation-duration)))
    (dissoc state :animation-start)
    state))

(defmethod draw-state :win-screen
  [{:keys [canvas-size game-state animation-start] :as state}]
  (draw-state game-state)
  (q/with-translation
    [0 (if animation-start
         (* (- 1 (easing-functions/ease-out-cubic
                  (/ (- (q/millis) animation-start) animation-duration)))
            canvas-size)
         0)]
    (q/stroke 60)
    (q/fill 80 30 230 230)
    (let [top-left (+ margin (* 0.1 canvas-size))
          width-height (- (* 0.8 canvas-size) (* 2 margin))]
      (q/rect top-left top-left width-height width-height (* 0.05 canvas-size)))
    (q/no-stroke)
    (q/fill 50)
    (q/text-size (* 0.06 canvas-size))
    (q/text-align :center :center)
    (q/text (if (:next-level game-state)
              "Yay! Sucess!"
              "You beat all the levels!")
            (* canvas-size 0.5)
            (* canvas-size 0.5))
    (buttons/draw-buttons state)))

(defmethod mouse-pressed :win-screen
  [{:keys [animation-start] :as state} {:keys [x y]}]
  (if-let [button-fn (when-not animation-start
                       (buttons/get-clicked-button-fn state x y))]
    (button-fn state)
    state))

(defmethod key-pressed :win-screen
  [{:keys [game-state] :as state} event]
  (case (:key event)
    :Escape (screen-transition/mk-state
             state
             {:type :main-menu}
             :left)
    :space (if (:next-level game-state)
             (screen-transition/mk-state state (:next-level game-state) :right)
             state)
    state))


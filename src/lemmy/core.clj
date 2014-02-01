(ns lemmy.core
  (use [clojure.string :only [join split]]
       [clojure.core.match :only [match]])
  (:gen-class))


;; make two groups in a parsing regex, the first is made up of either the numbers 2-10 or J,Q,K or A
;; The second group is one of h, s, d or c
;; The search is constrained to match both the beginning and end of the string,
;; meaning that the max allowable search string is length 3
;; A grouped query in clojure returns a vector, the 0th entry is the total matched string,
;; which we discard. We then utilize the value of the card and the suit in the next sanity
;; check, which is pretty self explanitory
(defn match-card [instr]
  (when-let [[_ value' suit] (re-find #"^([2-9JQKA]|10)([hsdc])$" instr)]
    (let [value (keyword value')]
     (match [suit]
       ["h"] (list value :hearts)
       ["s"] (list value :spades)
       ["c"] (list value :clubs)
       ["d"] (list value :diamonds)
       :else nil))))

;; the parser which produces hands to be analyzed by assess-hand
;; verifies that match-card didn't return a nil for any entry, as that would invalidate the hand
(defn parse-hand [instr]
  (let [result (map match-card (split instr #" "))]
    (if (some #(= nil %) result)
      '(:parse_error "tokenization failed")
      result)))

;; we're checking for pairs, so single cards don't matter, so discard them then
;; break the list of card values and occurrences up into something that we can easily pattern match
(defn find-pairs [nums]
  (let [pairs (->> nums
               (filter #(not= 1 (val %)))
               (partition-by val)
               (flatten)
               (vec))]
  (if (= 1 (count pairs)) ;; match for vectors is annoying in that it will only handle patterns of the same length
                          ;; so we break it up into two separate matches
    (match [pairs]
      [[a 2]]     {:result [:pair a] :text (format "pair of %s's" (name a))}
      [[a 3]]     {:result [:three a] :text (format "three %s's" (name a))}
      [[a 4]]     {:result [:four a] :text (format "four %s's" (name a))})

    (match [pairs]
      [[a 2 b 2]] {:result [:two-pair a b] :text (format "two pair : %s's and %s's" (name a) (name b))}
      [[a 2 b 3]] {:result [:full-house b a] :text (format "full house : %s's over %s's" (name b) (name a))}
      [[b 3 a 2]] {:result [:full-house b a] :text (format "full house : %s's over %s's" (name b) (name a))}))))

(defn keytonum [keyw]
    (match [keyw]
      [(:or :2 :3 :4 :5 :6 :7 :8 :9 :10)] (Integer. (name keyw))
      [:J] 11
      [:Q] 12
      [:K] 13
      [:A] 1))

(defn find-flush-straight [hand]
  hand)

(defn count-occurrences [hand]
  (reduce (partial merge-with +) {} (map #(assoc {} (first %) 1) hand)))

;;
;; the main poker logic function
;;
(defn assess-hand [hand]
  (let [nums (count-occurrences hand)]
    (pr-str nums)
    (if (> 5 (count nums))
      (find-pairs nums)
      (find-flush-straight hand))))

(defn -main [& args]
  (println "meh"))

(assess-hand (parse-hand "2h 3h 4h 5h 6h"))
(apply sorted-set-by #(< (keytonum (first %1)) (keytonum (first %2)))  (assess-hand (parse-hand "2h 3h 4h 5h 6h")))
(reduce #())

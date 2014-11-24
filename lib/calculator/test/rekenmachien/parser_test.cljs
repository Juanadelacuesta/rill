(ns rekenmachien.parser-test
  (:require-macros [cemerick.cljs.test :refer [are is deftest]])
  (:require [cemerick.cljs.test :as _]
            [rekenmachien.parser :as t]))

(deftest parse-decimals
  (are [tokens result] (= result (t/parse-decimals tokens))
       [1]           , [1]
       [1 2]         , [12]
       [1 :x 2]      , [1 :x 2]
       [:x 1 2]      , [:x 12]
       [1 2 :x 3 4]  , [12 :x 34]
       [1 2 :x]      , [12 :x]
       [1 :dot 2 :x] , [1.2 :x]))

(deftest parse-blocks
  (are [tokens result] (= result (t/parse-blocks tokens))
       [1 :x 2 :x 3]                     , [1 :x 2 :x 3]
       [:sin 1 :cos 2 :close 3 :close 4] , [[:sin 1 [:cos 2] 3] 4]))

(deftest parse-infix
  (are [tokens result] (= result (t/parse-infix tokens :op))
       [1 :op 2]         , [[:op 1 2]]
       [:x 1 :op :y]     , [:x [:op 1 :y]]
       [:op 1 2 :op 3 4] , [:op 1 [:op 2 3] 4]))

(deftest parse-special
  (are [tokens result] (= result (t/parse-special tokens {:x1 [:x 1] :x2 [:x 2]}))
       [1 :x1]     , [1 :x 1]
       [1 :x2]     , [1 :x 2]
       [1 :x1 :x2] , [1 :x 1 :x 2]
       [1 :x2 :x1] , [1 :x 2 :x 1]))

(deftest parse-opers
  (are [tokens result] (= result (t/parse-opers tokens))
       [1 :add 2]                              , [[:add 1 2]]
       [:neg 5 :x2]                            , [[:mul -1 [:pow 5 2]]]
       [:x 1 :add :y]                          , [:x [:add 1 :y]]
       [1 :add :neg 2 :mul 3]                  , [[:add 1 [:mul [:mul -1 2] 3]]]
       [1 :add 2 :add 3 :add 4]                , [[:add [:add [:add 1 2] 3] 4]]
       [1 :add 2 :mul 3 :add 4]                , [[:add [:add 1 [:mul 2 3]] 4]]
       [[:open 1 :add 2] :pow 3 :mul 4 :add 5] , [[:add [:mul [:pow [:open [:add 1 2]] 3] 4] 5]]))

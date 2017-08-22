# PUZZlE GAME
目前支持三个难度，入门：puzzle8, 中级：puzzle15, 高级：puzzle24
改编自OpenCV的 puzzle15 demo
### 先说一说算法方面
* 判断puzzle 是可解的 [puzzle8 is solvable](http://www.geeksforgeeks.org/check-instance-8-puzzle-solvable/)
    * 可解 inversions ＝ 10
    ```
        1   8   2
        *   4   3
        7   6   5
    ```
    * 无解 inversions ＝ 11
   ```
        8   1   2
        *   4   3
        7   6   5
    ``` 
    *  计数，依次遍历 每个数字，记录比当前这个数字的前面比它大的数字的数字的个数记录为inversions，如果inversions ％ 2 ＝＝ 0 ，就是可解的，这是因为移动是对称的，如果inversions是偶数，经过若干次移动，重新计算的inversions还是偶数
    *  
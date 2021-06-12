package com.example.studienarbeit_kugellabyrinth;

/** Template for a Database entry
 * @author Philip Bartmann
 * @version 1.0
 * @since 1.0
 */
public class DatabaseTemplate {

    final String TAG = "DatabaseTemplate";

    /** id of the entry => is autoincremented
     */
    public long id;
    /** name of the player
     */
    public String name;
    /** time the player needed to complete the maze
     */
    public String time;

    /** Creates an Database Template
     * @param name name of the player
     * @param time time the player needed to complete the maze
     */
    public DatabaseTemplate(String name, String time) {
        this.name          = name;
        this.time        = time;

        id = -1;
    }
}

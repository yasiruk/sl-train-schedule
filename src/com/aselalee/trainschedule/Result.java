/**
* @copyright	Copyright (C) 2011 Asela Leelaratne
* @license		GNU/GPL Version 3
* 
* This Application is released to the public under the GNU General Public License.
* 
* GNU/GPL V3 Extract.
* 15. Disclaimer of Warranty.
* THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
* EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES
* PROVIDE THE PROGRAM AS IS WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED,
* INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
* FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE
* PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL
* NECESSARY SERVICING, REPAIR OR CORRECTION.
*/

package com.aselalee.trainschedule;

public class Result {
	public String name; /* Train Name */
	public String arrivalTime; /* Arriving at starting station */
	public String depatureTime; /* Departing from starting station */
	public String arrivalAtDestinationTime; /* Arrival at destination */
	public String delayTime; /* Train delay information (Sometimes empty) */
	public String comment; /* Comments (Generally an empty string) */
	public String startStationName; /* Staring station of user */
	public String endStationName; /* Target destination of user */
	public String toTrStationName; /* Final destination of train*/
	public String fDescription; /* Train frequency */
	public String tyDescription; /* Train type */
	public String duration; /* Traveling time. Calculated locally. */
}

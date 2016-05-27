/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jenkins.plugins.office365connector;

import java.util.Arrays;

/**
 * Helper utilities
 */
public final class Utils
{
    /**
     * Determines if any of Strings specified is either null or empty.
     */
    public static boolean isEmpty( String ... strings )
    {
        if (( strings == null ) || ( strings.length < 1 )) {
            return true;
        }

        for ( String s : strings )
        {
            if (( s == null ) || ( s.trim().length() < 1 ))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Verifies neither of Strings specified is null or empty.
     * @return first String provided
     * @throws java.lang.IllegalArgumentException
     */
    public static String verifyNotEmpty( String ... strings )
    {
        if ( isEmpty( strings ))
        {
            throw new IllegalArgumentException( String.format(
                "Some String arguments are null or empty: %s", Arrays.toString( strings )));
        }

        return strings[ 0 ];
    }
}

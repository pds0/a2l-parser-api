# PSA - Outil Calibration Parser API
This API consists of parsing an [**A2l file**](https://www.asam.net/standards/detail/mcd-2-mc/wiki/#TechnicalContent) (.a2l) and a [**IntelHex file**](https://fr.wikipedia.org/wiki/HEX_(Intel)) (.hex) to return a set of **Characteristics**.

## A2L Objects
### Characteristic Object
_CHARACTERISTIC_ are a type of Object containing data stored in the IntelHex file. The types of data in a Characteristic handled by the Parser are : 

- **VALUE**, a single value  
- **VAL_BLK**, a set of value (like an array) 
-  **CURVE**, an associative array (like a key/value set) => has one axis 
-  **MAP**, an 2 dimensional associative array (with 2 keys for one value) => as two axis
> Not all *types of Characteristic* are handled by the Parser, so the other types not handled yet are **ASCII**, **CUBOID**, **CUBE_4**, **CUBE_5**.

Characteristic can be associated (or composed) with other [**A2l Objects**](https://www.asam.net/standards/detail/mcd-2-mc/wiki/#TechnicalContent ).


### Compu Method
  _COMPU_METHOD_, stores a type of function and its details. There are several types of COMPU_METHOD:
  - **RAT_FUNC**: 6-coefficient rational function with 2nd-degree numerator and denominator polynomials: $`\frac{ax^2+bx+c}{dx^2+ex+f}`$. The coefficients are included in the COMPU_METHOD via the keyword `COEFFS` (in nearly all case,  the coefficients a, d, e are equals to 0) .
  
> Thus to retrieve a physical values with a RAT_FUNC  we usually use this function : $`f^{-1}(x) = \frac{fx-c}{b}`$

  - **TAB_VERB**: verbal table (i.e. enumeration, a key/value set of Strings) which make a reference to another A2l object via the keyword `COMPU_TAB_REF`  :
    - _COMPUVTAB_ :  this A2l object consists of an enumeration of values 

> Not all *types of Compu_Method* are handled by the Parser, so the other types not handled yet are **IDENTICAL**, **LINEAR**, **TAB_INTP**, **TAB_NOINTP**, **FORM**.
  
### Record Layout
_RECORD_LAYOUT_ stores how the hexadecimal values parsed from the IntelHex file should be read for an A2l Object. It usually defines the type the hexadecimal values are :

|A2ML|    `ASAP2`		| Windows  |Explanation  |
|---|---|---|---|
|uchar|`UBYTE`			|BYTE | unsigned 8 Bit |
|char |`SBYTE`  		|char | signed 8 Bit    |
|uint |`UWORD`  		|WORD | unsigned integer 16 Bit  |
|int  |`SWORD`  		|int  | signed integer 16 Bit|
|ulong|`ULONG` 			|DWORD| unsigned integer 32 Bit|
|long |`SLONG`  		|LONG | signed integer 32 Bit |
|float|`FLOAT32_IEEE` 	|     | float 32 Bit|
	
> RecordLayouts also provides the information of whether a set of data is in row- or column-major order for MAP characteristic
	
    
### Axis

 _AXIS_DESCR_ are the main A2l objects, it stores all the information about an axis like its length (number of points), its _COMPU_METHOD_ to apply to the set of raw points retrieved from the IntelHex File, its type ... 

A type of an axis defines how the value of the axis are fetch or determined : 
 
|Axis Type|    Description		| How to retrieve the points|
|---|---|---|
|**STD_AXIS**|Axis specific to one table| All the information (number of points, the value of the points and the data ) are stored in the IntelHex file along with the data of the Characteristic at the address of the characteristic this way : <ul> <li> 1. the number of points </li> <li> 2. the axis points </li><li>bis. if it's a MAP: 2 axis <ul><li>1bis. the number of points of the second axis (if there is one)</li><li>2bis. the axis points of the second axis </li></ul> <li> 3. the data of characteristic </li> </ul>|
|**COM_AXIS**|Axis shared by various tables| This type of axis use a reference to another A2l object, the _AXIS_PTS_, with the key word `AXIS_PTS_REF`. This _AXIS_PTS_ has his own address to fetch the points of the axis in the IntelHex File in addition to the address of the characteristic to retrieve the data of the characteristic|
|**FIX_AXIS**|Axis specific to one table with calculated axis points. Axis points are not stored in ECU memory|This type of axis doesn't store the points of the axis in the IntelHex File but instead use a incremental function to get the points which can be of type : <ul><li>`FIX_AXIS_PAR` : Specifies the value of the first sample point, the power-of-two exponent of the increment value and total number of sample points for computing the sample point values of an equidistant axis of type _FIX_AXIS_</li><li>`FIX_AXIS_PAR_DIST` : Specifies the value of the first sample point, the increment value and the total number of sample points for computing the sample point values of an equidistant axis of type FIX_AXIS</li></ul>  |
---
> - The `how to get the points` column is subject to change because a lot of the mechanism to retrieve the points of the axis were deduced with a lot of internal testing
> - Not all *AXIS Types* are handled by the Parser, so the other types not handled yet are   **CURVE_AXIS**, **RES_AXIS**.

_AXIS_PTS_ are the object referenced in a _AXIS_DESCR_ of type COM_AXIS. Since this type of _AXIS_DESCR_ does state two addresses : one for the data in the Characteristic object and another one for the points of the axis in the _AXIS_PTS_ object,  each address has their own way to retrieve the data from the IntelHex file i.e. a Record Layout to parse hexadecimal value, a Compu Method to apply to the raw data, ... 
 

## Useful Documentation and Tool 
A wiki and glossary of objects from A2l :
https://www.asam.net/standards/detail/mcd-2-mc/wiki/

IntelHex Format explanation : 
https://fr.wikipedia.org/wiki/HEX_(Intel)

Third party Library for Parsing IntelHex :
https://github.com/j123b567/java-intelhex-parser

ASAP2 demo (a A2l parser program) :
http://jnachbaur.de/ASAP2Demo/ASAP2.html


## How the Parser works
The Parser works in 3 steps :

 1. It reads first the [**A2l file**](https://www.asam.net/standards/detail/mcd-2-mc/wiki/#TechnicalContent) line by line to extract the importants objects like : Characteristics, CompuMethods, RecordLayouts, CompuVTabs, AxisPoints. Those extracted objects are stored in collections (LinkedList or HashMap if the object needs to be referenced) .
 
 2. Then it reads the  [**IntelHex file**](https://fr.wikipedia.org/wiki/HEX_(Intel)) line by line to store the data in a HashMap by records of data associated with address ranges of 32 bytes(a pair of letter or digit = a byte) this way :
 >  A record : $`\underbrace{0\times08AE451D}_{\text{address}}   
\xrightarrow{association}   
\underbrace{\underbrace{E8}_{\text{1 byte}}BA008031323334353637383930010020010080F7BF008009000002F8BF0080E8}_{\text{32 or less bytes}}`$
 
 3. Finally, with the parsed A2l Objects and the IntelHex records from the files, it assign for each Characteristic a value of data depending on the specifications of the Characteristic like the type of data and the other A2l Object associated with.

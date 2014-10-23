#include "data.h"

/* declaration of the private method. */
int METH(myPrivateMethod)(int a);

int METH(myItf, myMethod)(int a, int b) {
	PRIVATE.a = a;
	PRIVATE.b = b;
	return CALL(myPrivateMethod)(b);
}

int METH(myPrivateMethod)(int a) {
	/* test MIND-133 */
	const char *c = "\x0D\x0A";
	return PRIVATE.a + c[0];
}

/* test MIND-130 */
typedef struct {

    /*
      The comment following this decl seems to trigger the bug.    Nuking any line in the comment makes the compile ok!
    */

    unsigned int anchor_non_anchor_ref_l1[4];
                                                            /*
                                                                then anchor_non_anchor_ref_l0 [] specifies the view_id of the view
                                                                component for inter-view prediction in the initialized RefPicList0
                                                                in decoding anchor view components with current VOIdx.
                                                               Otherwise(if anchor_pic_flag = 0),
                                                                the same for decoding non-anchor view components with current VOIdx.
                                                            */

} H264_MVCSpecificSetGlobalParamSPS_t;

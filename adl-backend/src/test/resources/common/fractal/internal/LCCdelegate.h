/**
 * Fractal Runtime
 *
 * Copyright (C) 2009 STMicroelectronics
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 *
 * Contact: mind@ow2.org
 *
 * Authors: Matthieu Leclercq
 */

#ifndef FRACTAL_INTERNAL_LCCDELEGATE
#define FRACTAL_INTERNAL_LCCDELEGATE

/* Include ErrorCode.idt directly since it contains only pure C constructs. */
#include "../api/ErrorCode.idt"

struct __component_LifeCycleState{
  int state;
};

int __component_getFcState_delegate(struct __component_LifeCycleState *state);

int __component_startFc_delegate(struct __component_LifeCycleState *state);

int __component_stopFc_delegate(struct __component_LifeCycleState *state);

#endif

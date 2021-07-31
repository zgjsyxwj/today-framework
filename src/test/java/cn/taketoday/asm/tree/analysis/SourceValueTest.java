// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package cn.taketoday.asm.tree.analysis;

import org.junit.jupiter.api.Test;
import cn.taketoday.asm.Opcodes;
import cn.taketoday.asm.tree.InsnNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SourceValue} tests.
 *
 * @author Eric Bruneton
 */
public class SourceValueTest {

  @Test
  public void testGetSize() {
    assertEquals(2, new SourceValue(2).getSize());
  }

  @Test
  public void testEquals() {
    SourceValue nullValue = null;

    boolean equalsSame = new SourceValue(1).equals(new SourceValue(1));
    boolean equalsValueWithDifferentSource =
        new SourceValue(1).equals(new SourceValue(1, new InsnNode(Opcodes.NOP)));
    boolean equalsValueWithDifferentValue = new SourceValue(1).equals(new SourceValue(2));
    boolean equalsNull = new SourceValue(1).equals(nullValue);

    assertTrue(equalsSame);
    assertFalse(equalsValueWithDifferentSource);
    assertFalse(equalsValueWithDifferentValue);
    assertFalse(equalsNull);
  }

  @Test
  public void testHashcode() {
    assertEquals(0, new SourceValue(1).hashCode());
    assertNotEquals(0, new SourceValue(1, new InsnNode(Opcodes.NOP)).hashCode());
  }
}

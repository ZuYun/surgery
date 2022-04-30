package sparkj.surgery.doctors.tryfinally

import groovyjarjarasm.asm.Opcodes
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter
import sparkj.surgery.JAPI
import sparkj.surgery.more.addLogCode
import sparkj.surgery.more.isReturn

open class TryFinalMethodAdapter(val process: MethodProcess, val className: String, methodVisitor: MethodVisitor?, access: Int, name: String?, descriptor: String?) :
    AdviceAdapter(JAPI, methodVisitor, access, name, descriptor) {

    private val beforeOriginalCode: Label = Label()
    private val afterOriginalCode: Label = Label()
    val methodName: String by lazy {
        name ?: "method_name"
    }

    override fun visitCode() {
        process.onMethodEnter(className, methodName, mv, this)
        mv.visitTryCatchBlock(
            beforeOriginalCode,
            afterOriginalCode,
            afterOriginalCode,
            null
        )
        mv.visitLabel(beforeOriginalCode)
        super.visitCode()
    }

    override fun visitInsn(opcode: Int) {
        if (opcode.isReturn()) {
            process.onMethodReturn(className, methodName, mv, this)
        }
        super.visitInsn(opcode)

    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        mv.visitLabel(afterOriginalCode)
        process.onMethodError(className, methodName, mv, this)
        mv.visitInsn(Opcodes.ATHROW)
        super.visitMaxs(maxStack, maxLocals)
    }
}

interface MethodProcess {
    fun onMethodEnter(className: String, methodName: String, mv: MethodVisitor, adapter: AdviceAdapter)

    fun onMethodReturn(className: String, methodName: String, mv: MethodVisitor, adapter: AdviceAdapter)

    fun onMethodError(className: String, methodName: String, mv: MethodVisitor, adapter: AdviceAdapter)
}
